package me.codexadrian.spirit.mixin;

import me.codexadrian.spirit.EngulfableItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements EngulfableItem {
    int engulfTime = 0;
    int maxEngulfTime = 0;

    private static final EntityDataAccessor<Boolean> RECIPE_OUTPUT = SynchedEntityData.defineId(ItemEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ENGULF_TIME = SynchedEntityData.defineId(ItemEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_ENGULF_TIME = SynchedEntityData.defineId(ItemEntity.class,
            EntityDataSerializers.INT);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineCorrupted(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(RECIPE_OUTPUT, false);
        builder.define(ENGULF_TIME, 0);
        builder.define(MAX_ENGULF_TIME, 0);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCorrupted(ValueInput input, CallbackInfo ci) {
        var entityData = ((ItemEntity) (Object) this).getEntityData();
        entityData.set(RECIPE_OUTPUT, input.getBooleanOr("isRecipeOutput", false));
        this.engulfTime = input.getIntOr("EngulfTime", 0);
        this.maxEngulfTime = input.getIntOr("MaxEngulfTime", 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveCorrupted(ValueOutput output, CallbackInfo ci) {
        var entityData = ((ItemEntity) (Object) this).getEntityData();
        output.putBoolean("IsRecipeOutput", entityData.get(RECIPE_OUTPUT));
        output.putInt("EngulfTime", engulfTime);
        output.putInt("MaxEngulfTime", maxEngulfTime);
    }

    @Override
    public void resetEngulfing() {
        engulfTime = 0;
        maxEngulfTime = 0;
    }

    @Override
    public void setMaxEngulfTime(int duration) {
        maxEngulfTime = duration;
    }

    @Override
    public boolean isEngulfed() {
        return engulfTime >= 0 && maxEngulfTime != 0;
    }

    @Override
    public boolean isFullyEngulfed() {
        return engulfTime >= maxEngulfTime;
    }

    @Override
    public boolean isRecipeOutput() {
        var entityData = ((ItemEntity) (Object) this).getEntityData();
        return entityData.get(RECIPE_OUTPUT);
    }

    @Override
    public void setRecipeOutput() {
        var entityData = ((ItemEntity) (Object) this).getEntityData();
        entityData.set(RECIPE_OUTPUT, true);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (!itemEntity.level().isClientSide()) {
            // Self-heal: strip the now-defunct "SpiritEngulfing" flag so previously-tainted items
            // (which carry an extra custom_data component) become stackable again.
            net.minecraft.world.item.ItemStack stack = itemEntity.getItem();
            net.minecraft.world.item.component.CustomData data =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null && data.contains("SpiritEngulfing")) {
                CompoundTag tag = data.copyTag();
                tag.remove("SpiritEngulfing");
                if (tag.isEmpty()) {
                    stack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                } else {
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));
                }
            }
        }
        if (isEngulfed() && !itemEntity.level().isClientSide()) {
            if (engulfTime % 5 == 0) {
                ServerLevel sLevel = (ServerLevel) itemEntity.level();
                sLevel.sendParticles(ParticleTypes.SOUL, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 10,
                        0.5, 0.5, 0.5, 0);
                sLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, itemEntity.getX(), itemEntity.getY(),
                        itemEntity.getZ(), 10, 0.5, 0.5, 0.5, 0);
            }
            engulfTime++;
        }
    }
}
