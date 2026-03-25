package me.codexadrian.spirit.blocks.blockentity;

import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.recipe.PedestalRecipe;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.utils.RecipeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
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

import java.util.ArrayList;

public class SoulPedestalBlockEntity extends BlockEntity {

    public EntityType<?> type;
    private ItemStack item = ItemStack.EMPTY;

    @Nullable
    public Entity entity;

    @Nullable
    public PedestalRecipe containedRecipe;
    public int burnTime = 0;
    public int age;

    public SoulPedestalBlockEntity(BlockPos $$1, BlockState $$2) {
        super(SpiritBlocks.SOUL_PEDESTAL_ENTITY.get(), $$1, $$2);
    }

    public static void tick(Level level1, BlockPos blockPos, BlockState blockState1, BlockEntity blockEntity) {
        if (blockEntity instanceof SoulPedestalBlockEntity soulPedestal) {
            soulPedestal.age = (soulPedestal.age + 1) % Integer.MAX_VALUE;
            if (soulPedestal.containedRecipe != null) {
                var recipe = soulPedestal.containedRecipe;
                if (!RecipeUtils.validatePedestals(blockPos, level1,
                        new ArrayList<>(recipe.ingredients()), false)) {
                    soulPedestal.setRecipe(null);
                    return;
                }
                if (soulPedestal.burnTime < recipe.duration()) {
                    for (int i = 0; i < 5; i++) {
                        if (soulPedestal.burnTime < recipe.duration() * .5) {
                            double percentage = 2 * soulPedestal.burnTime
                                    / (double) recipe.duration();
                            level1.addParticle(ParticleTypes.SOUL,
                                    blockPos.getX() + (3 * Math.sin(percentage * 2 * Math.PI)) + 0.5,
                                    blockPos.getY() + 0.75,
                                    blockPos.getZ() + (3 * Math.cos(percentage * 2 * Math.PI)) + 0.5,
                                    0,
                                    0,
                                    0);
                        } else {
                            double percentage = 2
                                    * ((soulPedestal.burnTime - recipe.duration() * .5)
                                            / (double) recipe.duration());
                            level1.addParticle(ParticleTypes.SOUL,
                                    blockPos.getX() + (3.0 * (1 - percentage) * Math.sin(percentage * 2 * Math.PI))
                                            + 0.5,
                                    blockPos.getY() + 0.75,
                                    blockPos.getZ() + (3.0 * (1 - percentage) * Math.cos(percentage * 2 * Math.PI))
                                            + 0.5,
                                    0,
                                    0,
                                    0);
                        }
                    }
                } else if (RecipeUtils.validatePedestals(blockPos, level1,
                        new ArrayList<>(recipe.ingredients()), true)) {
                    Entity entity = recipe.entityOutput().create(level1);
                    if (entity != null) {
                        if (recipe.outputNbt().isPresent())
                            entity.load(recipe.outputNbt().get());
                        entity.setPos(blockPos.getX() + 0.5, blockPos.getY() + 0.75, blockPos.getZ() + 0.5);
                        level1.addFreshEntity(entity);
                        for (int i = 0; i < 10; i++) {
                            level1.addParticle(ParticleTypes.SOUL, entity.getX(), entity.getY(), entity.getZ(), 0, 0,
                                    0);
                        }
                        soulPedestal.setType(null);
                    }
                    level1.sendBlockUpdated(blockPos, blockState1, blockState1, Block.UPDATE_ALL);
                    soulPedestal.setRecipe(null);
                }
                soulPedestal.burnTime++;
            }
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (compoundTag.contains("Soul")) {
            setType(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(compoundTag.getString("Soul"))));
        } else {
            setType(null);
        }
        if (compoundTag.contains("PedestalRecipe") && hasLevel()) {
            var recipe = PedestalRecipe.getEffect(compoundTag.getString("PedestalRecipe"),
                    getLevel().getRecipeManager());
            recipe.ifPresent(pedestalRecipe -> containedRecipe = pedestalRecipe);
        } else {
            containedRecipe = null;
        }
        burnTime = compoundTag.getInt("BurnTime");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (type != null) {
            compoundTag.putString("Soul", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        } else {
            compoundTag.remove("Soul");
        }
        if (containedRecipe != null) {
            compoundTag.putString("PedestalRecipe", "spirit:" + containedRecipe.hashCode());
        } else {
            compoundTag.remove("PedestalRecipe");
        }
        compoundTag.putInt("BurnTime", burnTime);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    public Entity getOrCreateEntity() {
        if (this.entity == null && this.hasLevel() && this.type != null) {
            this.entity = this.type.create(getLevel());
            if (entity instanceof Corrupted corrupted)
                corrupted.setCorrupted();
        }
        return entity;
    }

    public void setType(EntityType<?> type) {
        this.type = type;
        this.entity = null;
        this.setChanged();
    }

    public void setRecipe(@Nullable PedestalRecipe recipe) {
        this.containedRecipe = recipe;
        if (recipe == null)
            burnTime = 0;
        this.setChanged();
    }
}
