package me.codexadrian.spirit.blocks.blockentity;

import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulCageBlockEntity extends BlockEntity implements WorldlyContainer {

    public EntityType<?> type;
    private ItemStack soulCrystal = ItemStack.EMPTY;

    @Nullable
    public Entity entity;

    /** Game time until which the wand-inspector stats are shown above the cage; synced to clients. */
    public long inspectUntil = 0L;

    /**
     * When true the stats float above the cage continuously (a wand "pin"). Stored here rather than
     * on the wand because block-entity state syncs reliably to the client via {@link #getUpdateTag},
     * whereas held-item component changes were not reaching the renderer.
     */
    public boolean pinned = false;

    /** Client-side spawn-delay snapshot (+ the game time it was synced) for the wand "next spawn" countdown. */
    public int clientSpawnDelay = 0;
    public long clientSpawnDelaySyncTime = 0L;

    private final SoulCageSpawner enabledSpawner = new SoulCageSpawner(this);

    public SoulCageBlockEntity(BlockPos pos, BlockState state) {
        super(SpiritBlocks.SOUL_CAGE_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SoulCageBlockEntity blockEntity) {
        if (blockEntity.hasLevel() && !blockEntity.isEmpty()) {
            blockEntity.enabledSpawner.tick();
            // While pinned, re-sync the block entity to tracking clients every tick so the floating
            // stats update in real time (20 tps). sendBlockUpdated re-sends getUpdateTag without
            // marking the chunk dirty, so there is no per-tick disk-save cost.
            if (!level.isClientSide() && blockEntity.pinned) {
                level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean triggerEvent(int i, int j) {
        return this.enabledSpawner.onEventTriggered(i) || super.triggerEvent(i, j);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return soulCrystal.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? soulCrystal : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        var itemStack = removeItemNoUpdate(i);
        update(Block.UPDATE_ALL);
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        if (i == 0) {
            ItemStack crystal = soulCrystal;
            soulCrystal = ItemStack.EMPTY;

            return crystal;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, @NotNull ItemStack itemStack) {
        if (i == 0)
            soulCrystal = itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return worldPosition.distSqr(player.blockPosition()) <= 16;
    }

    @Override
    public void clearContent() {
        soulCrystal = ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        type = null;
        soulCrystal = input.read("crystal", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        inspectUntil = input.getLongOr("inspectUntil", 0L);
        pinned = input.getBooleanOr("pinned", false);
        clientSpawnDelay = input.getIntOr("spawnDelay", 0);
        clientSpawnDelaySyncTime = input.getLongOr("spawnDelaySync", 0L);
        setType();
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("crystal", ItemStack.OPTIONAL_CODEC, soulCrystal);
        output.putLong("inspectUntil", inspectUntil);
        output.putBoolean("pinned", pinned);
        output.putInt("spawnDelay", enabledSpawner.getSpawnDelay());
        output.putLong("spawnDelaySync", getLevel() != null ? getLevel().getGameTime() : 0L);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public void setType() {
        String soulCrystalType = SoulUtils.getSoulCrystalType(soulCrystal);
        if (soulCrystalType != null) {
            type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse(soulCrystalType));
        } else {
            type = null;
        }
    }

    public Entity getOrCreateEntity() {
        if (this.entity == null && this.getLevel() != null) {
            this.entity = this.type.create(getLevel(), EntitySpawnReason.TRIGGERED);
            if (entity instanceof Corrupted corrupted)
                corrupted.setCorrupted();
        }
        return entity;
    }

    public SoulCageSpawner getSpawner() {
        return this.enabledSpawner;
    }

    public void update(int update) {
        this.setChanged();
        if (this.getLevel() != null)
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), update);
    }

    @Override
    public int[] getSlotsForFace(@NotNull Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @NotNull ItemStack itemStack, @NotNull Direction direction) {
        return false;
    }
}
