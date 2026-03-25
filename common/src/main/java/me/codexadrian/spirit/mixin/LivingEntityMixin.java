package me.codexadrian.spirit.mixin;

import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.blocks.blockentity.PedestalBlockEntity;
import me.codexadrian.spirit.data.MobTraitData;
import me.codexadrian.spirit.data.traits.DamageTrait;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Corrupted {

    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final EntityDataAccessor<Boolean> CORRUPTED = SynchedEntityData.defineId(LivingEntity.class,
            EntityDataSerializers.BOOLEAN);

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineCorrupted(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(CORRUPTED, false);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCorrupted(ValueInput input, CallbackInfo ci) {
        if (input.getBooleanOr("Corrupted", false)) {
            setCorrupted();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveCorrupted(ValueOutput output, CallbackInfo ci) {
        if (isCorrupted()) {
            output.putBoolean("Corrupted", true);
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity victim = (LivingEntity) (Object) this;
        Corrupted corrupt = (Corrupted) victim;
        if (!victim.level().isClientSide()) {
            Entity entity = source.getEntity();
            if (entity instanceof Projectile projectile)
                entity = projectile.getOwner();
            if (entity instanceof Player player) {
                if (!victim.getType().is(Spirit.COLLECT_BLACKLISTED_TAG)) {
                    // canChangeDimensions() signature changed in 1.21 - now takes ServerLevel and
                    // BlockPos
                    // For soul collection, we just check if the entity is a valid target
                    if ((SpiritConfig.isCollectFromCorrupt() || !corrupt.isCorrupted())) {
                        boolean pedestalHasCrystal = false;
                        ItemStack crystal = ItemStack.EMPTY;
                        int radius = SpiritConfig.getSoulPedestalRadius();
                        AABB entityArea = victim.getBoundingBox().inflate(radius, 2, radius);
                        Optional<BlockPos> pedestalPos = BlockPos.betweenClosedStream(entityArea)
                                .filter(pos -> level().getBlockState(pos).is(SpiritBlocks.CRYSTAL_PEDESTAL.get()))
                                .map(BlockPos::immutable).findFirst();
                        if (pedestalPos.isPresent()
                                && level().getBlockEntity(pedestalPos.get()) instanceof PedestalBlockEntity pedestal
                                && !pedestal.isEmpty() && SoulUtils.canCrystalAcceptSoul(pedestal.getItem(0), victim)) {
                            crystal = pedestal.getItem(0);
                            pedestalHasCrystal = true;
                            pedestal.setChanged();
                            level().sendBlockUpdated(pedestalPos.get(), pedestal.getBlockState(),
                                    pedestal.getBlockState(), Block.UPDATE_ALL);
                        }
                        if (crystal.isEmpty() && !SoulUtils.canCrystalAcceptSoul(crystal, victim)) {
                            crystal = SoulUtils.findCrystal(player, victim, false, false, true);
                        }
                        if (!crystal.isEmpty()) {
                            if (crystal.is(SpiritItems.SOUL_CRYSTAL.get()))
                                SoulUtils.handleSoulCrystal(crystal, player, victim);
                            else if (crystal.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get()))
                                SoulUtils.handleCrudeSoulCrystal(crystal, player, victim);
                            else if (crystal.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
                                SoulUtils.handleMobCrystal(crystal, player, victim);
                            }
                            if (pedestalPos.isPresent() && pedestalHasCrystal) {
                                ServerLevel sLevel = (ServerLevel) player.level();
                                sLevel.sendParticles(ParticleTypes.SOUL, pedestalPos.get().getX() + 0.5,
                                        pedestalPos.get().getY() + 0.5, pedestalPos.get().getZ() + 0.5, 15, 0.5, 1, 0.5,
                                        0);
                                sLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pedestalPos.get().getX() + 0.5,
                                        pedestalPos.get().getY() + 0.5, pedestalPos.get().getZ() + 0.5, 15, 0.5, 1, 0.5,
                                        0);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isCorrupted() {
        return entityData.get(CORRUPTED);
    }

    @Override
    public void setCorrupted() {
        entityData.set(CORRUPTED, true);
    }

    @Inject(method = "getAttributeValue", at = @At("RETURN"), cancellable = true)
    public void getMobTraitDamage(net.minecraft.core.Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        // noinspection ConstantConditions
        if (attribute.value() == Attributes.ATTACK_DAMAGE && (Object) this instanceof Player player
                && player.getInventory() != null) {
            if (player.getMainHandItem().is(SpiritItems.SOUL_STEEL_AXE.get())
                    || player.getMainHandItem().is(SpiritItems.SOUL_STEEL_BLADE.get())) {
                if (SoulUtils.isEmpowered(player.getMainHandItem())) {
                    ItemStack soulCrystal = SoulUtils.findCrystal(player, null, true, true, false);
                    if (!soulCrystal.isEmpty()) {
                        String soulCrystalType = SoulUtils.getSoulCrystalType(soulCrystal);
                        if (soulCrystalType != null && SoulUtils.getSoulsInCrystal(soulCrystal) > 0) {
                            var entityEffect = BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(soulCrystalType))
                                    .map(net.minecraft.core.Holder::value)
                                    .flatMap(et -> MobTraitData.getEffectForEntity(et,
                                    ((ServerLevel) player.level()).recipeAccess()));
                            if (entityEffect.isPresent()) {
                                int damage = 0;
                                for (var trait : entityEffect.get().traits()) {
                                    if (trait instanceof DamageTrait damageTrait) {
                                        damage += damageTrait.additionalDamage();
                                    }
                                }
                                cir.setReturnValue(cir.getReturnValueD() + damage);
                            }
                        }
                    }
                }
            }
        }
    }
}
