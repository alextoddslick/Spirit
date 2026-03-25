package me.codexadrian.spirit.blocks;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.data.Tier; // Added missing import
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.core.BlockPos;

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

        if (level.getBlockEntity(pos) instanceof SoulCageBlockEntity soulSpawner) {
            System.out.println(
                    "SoulCage: Interaction. Empty: " + soulSpawner.isEmpty() + ", Shift: " + player.isShiftKeyDown());
            if (soulSpawner.isEmpty()) {
                boolean canAccept = SoulUtils.canCrystalBeUsedInCage(stack);
                Tier tier = SoulUtils.getTier(stack, level);
                System.out.println("SoulCage: Can Accept: " + canAccept + ", Tier: "
                        + (tier != null ? tier.displayName() : "null"));

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
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
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
