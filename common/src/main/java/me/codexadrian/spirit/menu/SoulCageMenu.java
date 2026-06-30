package me.codexadrian.spirit.menu;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.registry.SpiritMenus;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Upgrade menu for a Soul Cage. Two dual input slots tune the cage: the soul steel slot (block OR ingot)
 * raises the spawn range and the netherite slot (block OR ingot) shortens the spawn delay (floored at
 * {@value #MIN_DELAY_FLOOR} ticks). A block is
 * worth {@value #INGOTS_PER_BLOCK} ingots. Range cost is <b>exponential</b> (it doubles each level), so
 * soul steel is banked on the cage until it can afford the next level. Netherite cuts the spawn delay:
 * a block takes 1.8s off the max delay and 0.2s off the min, an ingot takes 0.2s off the max. The +/-
 * toggle drives Confirm: "+" banks soul steel / applies netherite, "-" lowers the spawn range (adding 1s
 * to the max delay, no refund). Reset wipes everything. Broken cages refund invested materials — see
 * SoulCageBlock.
 */
public class SoulCageMenu extends AbstractContainerMenu {

    public static final int STEEL_SLOT = 0;
    public static final int NETHERITE_SLOT = 1;
    public static final int BUTTON_RESET = 0;
    public static final int BUTTON_CONFIRM = 1;   // + mode: bank soul steel / apply netherite
    public static final int BUTTON_DECREASE = 2;  // - mode: lower spawn range (adds delay), no refund

    /** Crafting ratio: 9 ingots make one block, for both soul steel and netherite. */
    public static final int INGOTS_PER_BLOCK = 9;
    public static final int RANGE_BASE_BLOCKS = 1;                    // soul steel blocks for the first +1 range
    public static final int MAX_RANGE_BONUS = 16;
    public static final int MIN_DELAY_FLOOR = 20;                     // 1.0s floor on both min and max delay

    // Netherite reduces spawn delay (20 ticks = 1s). Block: -1.8s max & -0.2s min; ingot: -0.2s max only.
    public static final int NETHERITE_INGOT_MAX_TICKS = 4;           // 0.2s off max delay per ingot
    public static final int NETHERITE_BLOCK_MAX_TICKS = 36;          // 1.8s off max delay per block
    public static final int NETHERITE_BLOCK_MIN_TICKS = 4;           // 0.2s off min delay per block
    public static final int RANGE_DECREASE_DELAY_TICKS = 20;         // +1.0s onto max delay per range step removed

    /**
     * Soul steel cost (ingot-equiv) to reach range {@code level}; doubles each positive level. Levels at or
     * below the tier default (0 or negative, from lowering range) all cost the base amount, so restoring
     * lost range is cheap — and a negative shift can never produce a garbage value.
     */
    public static int rangeStepIngots(int level) {
        int clamped = Math.max(1, Math.min(level, MAX_RANGE_BONUS));
        return (RANGE_BASE_BLOCKS * INGOTS_PER_BLOCK) << (clamped - 1);
    }

    /** Total soul steel (ingot-equiv) sunk into reaching {@code bonus} positive range levels (geometric sum). */
    public static int totalRangeIngots(int bonus) {
        int clamped = Math.max(0, Math.min(bonus, MAX_RANGE_BONUS));
        return (RANGE_BASE_BLOCKS * INGOTS_PER_BLOCK) * ((1 << clamped) - 1);
    }

    private final SoulCageBlockEntity cage;
    private final ContainerLevelAccess access;
    private final Container upgradeSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };

    /** Resolves the cage from the (synced) block position; works on both server and client. */
    public SoulCageMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(SpiritMenus.SOUL_CAGE_MENU.get(), containerId);
        this.cage = (SoulCageBlockEntity) inventory.player.level().getBlockEntity(pos);
        this.access = ContainerLevelAccess.create(cage.getLevel(), cage.getBlockPos());

        // Two dual slots, centered on x=92: soul steel (range, left) and netherite (spawn time, right).
        // Each accepts that material's block OR ingot form.
        Item soulSteelBlock = SpiritBlocks.SOUL_STEEL_BLOCK.get().asItem();
        Item soulSteelIngot = SpiritItems.SOUL_STEEL.get();
        addSlot(new FilterSlot(upgradeSlots, STEEL_SLOT, 65, 65,
                stack -> stack.is(soulSteelBlock) || stack.is(soulSteelIngot)));
        addSlot(new FilterSlot(upgradeSlots, NETHERITE_SLOT, 103, 65,
                stack -> stack.is(Items.NETHERITE_BLOCK) || stack.is(Items.NETHERITE_INGOT)));

        // Player inventory + hotbar, centered in the wide lower panel (grid is 162 wide -> x=12).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 12 + col * 18, 120 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 12 + col * 18, 180));
        }
    }

    public SoulCageBlockEntity getCage() {
        return cage;
    }

    /**
     * "+" mode: banks the soul steel slot toward the exponentially-priced next range level (buying as many
     * levels as the bank can afford) and consumes netherite to cut the spawn delay. Returns whether anything
     * changed.
     */
    private boolean applyUpgrades(Level level) {
        boolean changed = false;

        // Soul steel: bank the whole slot, then buy exponentially-priced range levels from the bank.
        ItemStack steel = upgradeSlots.getItem(STEEL_SLOT);
        if (!steel.isEmpty() && cage.spawnRangeBonus < MAX_RANGE_BONUS) {
            int perItem = steel.is(SpiritBlocks.SOUL_STEEL_BLOCK.get().asItem()) ? INGOTS_PER_BLOCK : 1;
            cage.pendingSteelIngots += steel.getCount() * perItem;
            steel.setCount(0);
            changed = true;
        }
        while (cage.spawnRangeBonus < MAX_RANGE_BONUS
                && cage.pendingSteelIngots >= rangeStepIngots(cage.spawnRangeBonus + 1)) {
            cage.pendingSteelIngots -= rangeStepIngots(cage.spawnRangeBonus + 1);
            cage.spawnRangeBonus++;
            // Raising range back undoes the spawn-delay penalty that lowering it added.
            cage.rangeDelayPenaltyTicks = Math.max(0, cage.rangeDelayPenaltyTicks - RANGE_DECREASE_DELAY_TICKS);
            changed = true;
        }

        // Netherite: each item cuts the max delay (blocks also cut min), down to the 1.0s floor.
        changed |= applyNetherite(SoulUtils.getTier(cage.getItem(0), level));

        if (changed) {
            cage.update(Block.UPDATE_ALL);
        }
        return changed;
    }

    /** Consumes netherite item-by-item, reducing the max (and, for blocks, min) spawn delay within the floor. */
    private boolean applyNetherite(Tier tier) {
        ItemStack neth = upgradeSlots.getItem(NETHERITE_SLOT);
        if (neth.isEmpty() || tier == null) {
            return false;
        }
        boolean isBlock = neth.is(Items.NETHERITE_BLOCK);
        int maxCap = Math.max(0, tier.maxSpawnDelay() - MIN_DELAY_FLOOR);
        int minCap = Math.max(0, tier.minSpawnDelay() - MIN_DELAY_FLOOR);
        int maxStep = isBlock ? NETHERITE_BLOCK_MAX_TICKS : NETHERITE_INGOT_MAX_TICKS;
        int minStep = isBlock ? NETHERITE_BLOCK_MIN_TICKS : 0;
        boolean changed = false;
        while (neth.getCount() > 0) {
            boolean maxRoom = cage.maxDelayReductionTicks < maxCap;
            boolean minRoom = isBlock && cage.minDelayReductionTicks < minCap;
            if (!maxRoom && !minRoom) {
                break; // this item can't reduce anything further
            }
            cage.maxDelayReductionTicks = Math.min(maxCap, cage.maxDelayReductionTicks + maxStep);
            if (isBlock) {
                cage.minDelayReductionTicks = Math.min(minCap, cage.minDelayReductionTicks + minStep);
            }
            neth.shrink(1);
            changed = true;
        }
        return changed;
    }

    /**
     * "-" mode: lowers the spawn range by one and adds {@value #RANGE_DECREASE_DELAY_TICKS} ticks (1s) onto
     * the max spawn delay. No materials are removed or refunded. Stops once the effective range hits 0.
     */
    private boolean applyRangeDecrease(Level level) {
        Tier tier = SoulUtils.getTier(cage.getItem(0), level);
        int baseRange = tier != null ? tier.spawnRange() : 0;
        if (baseRange + cage.spawnRangeBonus <= 0) {
            return false;
        }
        cage.spawnRangeBonus--;
        cage.rangeDelayPenaltyTicks += RANGE_DECREASE_DELAY_TICKS;
        cage.update(Block.UPDATE_ALL);
        return true;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id == BUTTON_CONFIRM) {
            access.execute((level, pos) -> {
                if (!level.isClientSide() && !applyUpgrades(level)) {
                    player.displayClientMessage(Component.literal("Add soul steel or netherite, then Confirm.")
                            .withStyle(ChatFormatting.GRAY), true);
                }
            });
            return true;
        }
        if (id == BUTTON_DECREASE) {
            access.execute((level, pos) -> {
                if (!level.isClientSide() && !applyRangeDecrease(level)) {
                    player.displayClientMessage(Component.literal("Spawn range is already at the minimum.")
                            .withStyle(ChatFormatting.GRAY), true);
                }
            });
            return true;
        }
        if (id == BUTTON_RESET) {
            access.execute((level, pos) -> {
                if (!level.isClientSide()) {
                    cage.spawnRangeBonus = 0;
                    cage.pendingSteelIngots = 0;
                    cage.maxDelayReductionTicks = 0;
                    cage.minDelayReductionTicks = 0;
                    cage.rangeDelayPenaltyTicks = 0;
                    cage.update(Block.UPDATE_ALL);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            int playerStart = NETHERITE_SLOT + 1;
            int playerEnd = slots.size();
            if (index <= NETHERITE_SLOT) {
                // Upgrade slot -> player inventory.
                if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(SpiritBlocks.SOUL_STEEL_BLOCK.get().asItem()) || stack.is(SpiritItems.SOUL_STEEL.get())) {
                if (!moveItemStackTo(stack, STEEL_SLOT, STEEL_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(Items.NETHERITE_BLOCK) || stack.is(Items.NETHERITE_INGOT)) {
                if (!moveItemStackTo(stack, NETHERITE_SLOT, NETHERITE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        // Return any leftover (un-consumed) resources so nothing is lost on close.
        clearContainer(player, upgradeSlots);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, SpiritBlocks.SOUL_CAGE.get());
    }

    /** A slot restricted by a predicate, holding the player's pending upgrade resources. */
    private static class FilterSlot extends Slot {
        private final java.util.function.Predicate<ItemStack> filter;

        FilterSlot(Container container, int index, int x, int y, java.util.function.Predicate<ItemStack> filter) {
            super(container, index, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return filter.test(stack);
        }
    }
}
