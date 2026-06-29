package me.codexadrian.spirit.blocks;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.menu.SoulCageMenu;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.data.Tier; // Added missing import
import me.codexadrian.spirit.utils.SoulUtils;
import me.codexadrian.spirit.platform.fabric.Services;
import me.codexadrian.spirit.SpiritConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

public class SoulCageBlock extends BaseEntityBlock {

    public static final MapCodec<SoulCageBlock> CODEC = simpleCodec(SoulCageBlock::new);

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public SoulCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return SpiritBlocks.SOUL_CAGE_ENTITY.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
            @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, SpiritBlocks.SOUL_CAGE_ENTITY.get(), SoulCageBlockEntity::tick);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {

        if (!(level.getBlockEntity(pos) instanceof SoulCageBlockEntity soulSpawner)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Soul Steel Wand interactions.
        if (stack.is(SpiritItems.SOUL_STEEL_WAND.get())) {
            if (soulSpawner.isEmpty()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            ItemStack caged = soulSpawner.getItem(0);
            String cagedType = SoulUtils.getSoulCrystalType(caged);

            // Note: shift-right-click "pin" is handled in SoulSteelWand#useOn, not here — vanilla's
            // sneak bypass skips block interaction when sneaking with a non-empty hand, so this
            // method is never reached for a shift-click while holding the wand.

            // Off-hand empty soul crystal: extract a level from the caged crystal.
            ItemStack offhand = player.getOffhandItem();
            if (offhand.is(SpiritItems.SOUL_CRYSTAL.get())
                    && SoulUtils.getSoulsInCrystal(offhand) == 0 && SoulUtils.getSoulCrystalType(offhand) == null
                    && caged.is(SpiritItems.SOUL_CRYSTAL.get()) && cagedType != null
                    && SoulUtils.getSoulsInCrystal(caged) > 0) {
                if (!level.isClientSide) {
                    extractCrystal(soulSpawner, caged, cagedType, offhand, player, level);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            // Open the cage upgrade/stats menu (range via soul steel blocks, spawn time via netherite).
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                Services.PLATFORM.openSoulCageMenu(serverPlayer, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Empty soul crystal: extract a soul crystal, dropping the caged crystal a whole level.
        if (!soulSpawner.isEmpty() && !player.isShiftKeyDown()
                && stack.is(SpiritItems.SOUL_CRYSTAL.get())
                && SoulUtils.getSoulsInCrystal(stack) == 0 && SoulUtils.getSoulCrystalType(stack) == null) {
            ItemStack caged = soulSpawner.getItem(0);
            String type = SoulUtils.getSoulCrystalType(caged);
            if (caged.is(SpiritItems.SOUL_CRYSTAL.get()) && type != null && SoulUtils.getSoulsInCrystal(caged) > 0) {
                if (!level.isClientSide) {
                    extractCrystal(soulSpawner, caged, type, stack, player, level);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (soulSpawner.isEmpty()) {
            boolean canAccept = SoulUtils.canCrystalBeUsedInCage(stack);
            Tier tier = SoulUtils.getTier(stack, level);
            if (canAccept && tier != null) {
                soulSpawner.entity = null;
                soulSpawner.setItem(0, stack.copy());
                if (!player.getAbilities().instabuild) {
                    stack.setCount(0);
                }
                soulSpawner.setType();
                soulSpawner.update(Block.UPDATE_ALL);
                return ItemInteractionResult.SUCCESS;
            }
        } else if (player.isShiftKeyDown()) {
            // Creative: pull the exact caged crystal out, untouched.
            if (player.getAbilities().instabuild) {
                soulSpawner.entity = null;
                soulSpawner.type = null;
                ItemStack exact = soulSpawner.removeItemNoUpdate(0);
                player.getInventory().placeItemBackInInventory(exact);
                soulSpawner.extractArmedUntil = 0L;
                soulSpawner.update(Block.UPDATE_ALL);
                return ItemInteractionResult.SUCCESS;
            }
            // Survival: two-step confirm. The first shift-right-click warns and arms a short window;
            // a second click within it forces the crystal out at a one-tier penalty.
            if (!level.isClientSide) {
                if (level.getGameTime() >= soulSpawner.extractArmedUntil) {
                    soulSpawner.extractArmedUntil = level.getGameTime() + 100L; // ~5s confirm window
                    player.displayClientMessage(Component.literal(
                            "Forcing the crystal out will cost one full tier. Shift-right-click again to confirm.")
                            .withStyle(ChatFormatting.RED), true);
                } else {
                    soulSpawner.extractArmedUntil = 0L;
                    extractFullCrystalWithPenalty(soulSpawner, player, level);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Extracts a soul crystal from the cage using an empty soul crystal. The caged crystal drops a
     * whole tier and its progress resets to that tier's threshold; the removed souls go into the
     * extracted crystal. In creative the full soul count is extracted and the cage is left untouched
     * (debug).
     */
    private static void extractCrystal(SoulCageBlockEntity cage, ItemStack caged, String type,
            ItemStack emptyCrystal, Player player, Level level) {
        int oldSouls = SoulUtils.getSoulsInCrystal(caged);

        // Creative: hand over a full-soul copy and leave the cage untouched (debug).
        if (player.getAbilities().instabuild) {
            ItemStack copy = new ItemStack(SpiritItems.SOUL_CRYSTAL.get());
            SoulUtils.deviateSoulCount(copy, oldSouls, level, type);
            player.getInventory().placeItemBackInInventory(copy);
            return;
        }

        // Threshold of the tier one level below the current tier, honoring per-type blacklists
        // (stays 0 when there is no lower tier the crystal's type can occupy).
        Tier current = SoulUtils.getTier(caged, level);
        int curReq = current != null ? current.requiredSouls() : 0;
        int prevReq = 0;
        for (Tier t : Tier.getTiers(level)) {
            if (t.blacklist().contains(type)) {
                continue;
            }
            int r = t.requiredSouls();
            if (r < curReq && r > prevReq) {
                prevReq = r;
            }
        }

        emptyCrystal.shrink(1);
        ItemStack extracted = new ItemStack(SpiritItems.SOUL_CRYSTAL.get());
        // Souls are conserved: the extracted crystal gets everything removed from the cage.
        SoulUtils.deviateSoulCount(extracted, prevReq <= 0 ? oldSouls : (oldSouls - prevReq), level, type);
        player.getInventory().placeItemBackInInventory(extracted);

        if (prevReq <= 0) {
            // No lower tier to drop to: the whole crystal came out, so empty the cage entirely.
            cage.setItem(0, ItemStack.EMPTY);
        } else {
            // Drop the caged crystal a whole level, resetting its progress to the new tier threshold.
            SoulUtils.deviateSoulCount(caged, prevReq - oldSouls, level, type);
        }

        cage.entity = null;
        cage.inspectUntil = level.getGameTime() + 100L;
        cage.setType();
        cage.update(Block.UPDATE_ALL);
    }

    /**
     * Survival "force extract": pulls the whole caged crystal out but knocks it down one full tier as
     * the cost (its souls are reset to the previous tier's threshold, honoring per-type blacklists).
     * The cage is left empty. At the lowest tier there is no lower threshold, so the crystal comes out
     * drained to 0 souls.
     */
    private static void extractFullCrystalWithPenalty(SoulCageBlockEntity cage, Player player, Level level) {
        ItemStack caged = cage.getItem(0);
        String type = SoulUtils.getSoulCrystalType(caged);
        int oldSouls = SoulUtils.getSoulsInCrystal(caged);

        Tier current = SoulUtils.getTier(caged, level);
        int curReq = current != null ? current.requiredSouls() : 0;
        int prevReq = 0;
        for (Tier t : Tier.getTiers(level)) {
            if (type != null && t.blacklist().contains(type)) {
                continue;
            }
            int r = t.requiredSouls();
            if (r < curReq && r > prevReq) {
                prevReq = r;
            }
        }

        cage.entity = null;
        cage.type = null;
        ItemStack extracted = cage.removeItemNoUpdate(0);
        // Drop the extracted crystal a whole tier: souls -> previous tier threshold (0 at the bottom).
        SoulUtils.deviateSoulCount(extracted, prevReq - oldSouls, level, type);
        player.getInventory().placeItemBackInInventory(extracted);

        cage.setType();
        cage.update(Block.UPDATE_ALL);
    }

    /** Concise crystal stats for the action-bar readout when inspecting with the wand. */
    private static Component inspectSummary(ItemStack caged, SoulCageBlockEntity cage, Level level) {
        int souls = SoulUtils.getSoulsInCrystal(caged);
        Tier tier = SoulUtils.getTier(caged, level);
        Tier next = SoulUtils.getNextTier(caged, level);
        Component name = cage.type != null ? cage.type.getDescription() : caged.getHoverName();
        Component tierComp = Component
                .translatable(tier == null ? SpiritConfig.getInitialTierName() : tier.displayName());
        String soulsStr = souls + (next != null ? " / " + next.requiredSouls() : "") + " souls";
        String extra = "";
        if (tier != null) {
            int secs = Math.max(0, cage.getSpawner().getSpawnDelay()) / 20;
            extra = "  next ~" + secs + "s  x" + tier.spawnCount() + "  r" + tier.spawnRange();
        }
        return Component.empty()
                .append(name)
                .append(Component.literal("  "))
                .append(Component.translatable("misc.spirit.tier", tierComp))
                .append(Component.literal("  " + soulsStr + extra));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {

        if (level.getBlockEntity(pos) instanceof SoulCageBlockEntity soulSpawner) {
            if (!soulSpawner.isEmpty() && player.isShiftKeyDown()) {
                soulSpawner.entity = null;
                soulSpawner.type = null;
                ItemStack divineCrystal = soulSpawner.removeItemNoUpdate(0);
                player.getInventory().placeItemBackInInventory(divineCrystal);
                soulSpawner.update(Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.@NotNull Builder builder) {
        // The cage can only be retrieved with a soul steel pickaxe enchanted with Silk Touch; any other
        // tool breaks it for nothing. This gates the cage, the caged crystal, and the upgrade refund.
        List<ItemStack> drops = new ArrayList<>();
        BlockEntity blockE = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
        if (!(blockE instanceof SoulCageBlockEntity cage) || !canHarvestCage(tool, builder.getLevel())) {
            return drops;
        }

        drops.add(new ItemStack(this));     // the cage itself
        drops.add(cage.getItem(0));          // the caged soul crystal
        // Refund invested soul steel: the exponential cost of every range level still installed, plus the bank.
        int steelIngots = SoulCageMenu.totalRangeIngots(Math.max(0, cage.spawnRangeBonus)) + cage.pendingSteelIngots;
        addStacks(drops, SpiritBlocks.SOUL_STEEL_BLOCK.get().asItem(), steelIngots / SoulCageMenu.INGOTS_PER_BLOCK);
        addStacks(drops, SpiritItems.SOUL_STEEL.get(), steelIngots % SoulCageMenu.INGOTS_PER_BLOCK);
        // Refund netherite: blocks are the only source of min-delay reduction, ingots cover the rest of max.
        int netheriteBlocks = cage.minDelayReductionTicks / SoulCageMenu.NETHERITE_BLOCK_MIN_TICKS;
        int ingotMaxTicks = cage.maxDelayReductionTicks - netheriteBlocks * SoulCageMenu.NETHERITE_BLOCK_MAX_TICKS;
        int netheriteIngots = Math.max(0, ingotMaxTicks) / SoulCageMenu.NETHERITE_INGOT_MAX_TICKS;
        addStacks(drops, Items.NETHERITE_BLOCK, netheriteBlocks);
        addStacks(drops, Items.NETHERITE_INGOT, netheriteIngots);
        return drops;
    }

    /** True only for a soul steel pickaxe carrying Silk Touch. */
    private static boolean canHarvestCage(ItemStack tool, ServerLevel level) {
        if (tool == null || !tool.is(SpiritItems.SOUL_STEEL_PICKAXE.get())) {
            return false;
        }
        Holder<Enchantment> silkTouch = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0;
    }

    /** Adds {@code count} of {@code item} to {@code drops}, split into 64-item stacks. */
    private static void addStacks(List<ItemStack> drops, Item item, int count) {
        while (count > 0) {
            int n = Math.min(count, 64);
            drops.add(new ItemStack(item, n));
            count -= n;
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter,
            @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }
}
