package me.codexadrian.spirit.blocks;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.data.Tier; // Added missing import
import me.codexadrian.spirit.utils.SoulUtils;
import me.codexadrian.spirit.SpiritConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    protected @NotNull InteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {

        if (!(level.getBlockEntity(pos) instanceof SoulCageBlockEntity soulSpawner)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // Soul Steel Wand: inspect the caged crystal, or extract a level if an empty soul crystal
        // is held in the off hand.
        if (stack.is(SpiritItems.SOUL_STEEL_WAND.get())) {
            if (soulSpawner.isEmpty()) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            ItemStack caged = soulSpawner.getItem(0);
            String cagedType = SoulUtils.getSoulCrystalType(caged);
            ItemStack offhand = player.getOffhandItem();
            if (offhand.is(SpiritItems.SOUL_CRYSTAL.get())
                    && SoulUtils.getSoulsInCrystal(offhand) == 0 && SoulUtils.getSoulCrystalType(offhand) == null
                    && caged.is(SpiritItems.SOUL_CRYSTAL.get()) && cagedType != null
                    && SoulUtils.getSoulsInCrystal(caged) > 0) {
                if (!level.isClientSide) {
                    extractCrystal(soulSpawner, caged, cagedType, offhand, player, level);
                }
                return InteractionResult.SUCCESS;
            }
            // Inspect: server sets+syncs the timer (floating text) and sends a readout to the action bar.
            if (!level.isClientSide) {
                soulSpawner.inspectUntil = level.getGameTime() + 100L;
                soulSpawner.update(Block.UPDATE_ALL);
                player.displayClientMessage(inspectSummary(caged, soulSpawner, level), true);
            }
            return InteractionResult.SUCCESS;
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
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
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
                return InteractionResult.SUCCESS;
            }
        } else if (player.isShiftKeyDown()) {
            soulSpawner.entity = null;
            soulSpawner.type = null;
            ItemStack divineCrystal = soulSpawner.removeItemNoUpdate(0);
            player.getInventory().placeItemBackInInventory(divineCrystal);
            soulSpawner.update(Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
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

    /** Concise crystal stats for the action-bar readout when inspecting with the wand. */
    private static Component inspectSummary(ItemStack caged, SoulCageBlockEntity cage, Level level) {
        int souls = SoulUtils.getSoulsInCrystal(caged);
        Tier tier = SoulUtils.getTier(caged, level);
        Tier next = SoulUtils.getNextTier(caged, level);
        Component name = cage.type != null ? cage.type.getDescription() : caged.getHoverName();
        Component tierComp = Component
                .translatable(tier == null ? SpiritConfig.getInitialTierName() : tier.displayName());
        String soulsStr = souls + (next != null ? " / " + next.requiredSouls() : "") + " souls";
        return Component.empty()
                .append(name)
                .append(Component.literal("  "))
                .append(Component.translatable("misc.spirit.tier", tierComp))
                .append(Component.literal("  " + soulsStr));
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
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.@NotNull Builder builder) {
        List<ItemStack> drops = super.getDrops(blockState, builder);
        BlockEntity blockE = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockE instanceof SoulCageBlockEntity) {
            drops.add(((SoulCageBlockEntity) blockE).getItem(0));
        }

        return drops;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter,
            @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }
}
