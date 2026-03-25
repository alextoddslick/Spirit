package me.codexadrian.spirit.blocks;

import me.codexadrian.spirit.blocks.blockentity.PedestalBlockEntity;
import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.registry.SpiritBlocks;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import com.mojang.serialization.MapCodec;

public class PedestalBlock extends BaseEntityBlock {

    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 3, 4, 12, 6, 12),
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(2, 6, 2, 14, 9, 14),
            Block.box(4, 9, 4, 12, 10, 12));

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, @NotNull BlockState blockState,
            @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player,
            @NotNull InteractionHand interactionHand, @NotNull BlockHitResult blockHitResult) {
        if (interactionHand != InteractionHand.OFF_HAND) {
            ItemStack itemStack = player.getMainHandItem();
            if (level.getBlockEntity(blockPos) instanceof PedestalBlockEntity soulPedestal) {
                if (soulPedestal.isEmpty()) {
                    soulPedestal.setItem(0, itemStack.copy());
                    if (!player.getAbilities().instabuild)
                        itemStack.setCount(0);
                    soulPedestal.update(Block.UPDATE_ALL);
                    return InteractionResult.SUCCESS;
                } else if (itemStack.isEmpty()) {
                    ItemStack soulCrystal = soulPedestal.removeItemNoUpdate(0);
                    player.getInventory().placeItemBackInInventory(soulCrystal);
                    soulPedestal.update(Block.UPDATE_ALL);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState blockState,
            @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, SpiritBlocks.PEDESTAL_ENTITY.get(), PedestalBlockEntity::tick);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.@NotNull Builder builder) {
        List<ItemStack> drops = super.getDrops(blockState, builder);
        BlockEntity blockE = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockE instanceof PedestalBlockEntity pedestalBlock) {
            drops.add(pedestalBlock.getItem(0));
        }

        return drops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new PedestalBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter,
            @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return SHAPE;
    }
}
