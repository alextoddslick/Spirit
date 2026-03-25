package me.codexadrian.spirit.data.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.data.MobTraitSerializer;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.MobTrait;
import me.codexadrian.spirit.data.MobTraitSerializer;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.entity.SoulArrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;

public record PotionTrait(List<MobEffectInstance> effects) implements MobTrait<PotionTrait> {
    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public void initializeArrow(SoulArrowEntity soulArrow) {
        for (MobEffectInstance effect : effects()) {
            soulArrow.addEffect(new MobEffectInstance(effect));
        }
    }

    @Override
    public void onHitBlock(ToolType type, Entity entity, BlockState blockState, Level level, BlockPos pos) {
        AreaEffectCloud potionCloud = EntityType.AREA_EFFECT_CLOUD.create(entity.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (potionCloud == null)
            return;
        for (var effect : effects()) {
            potionCloud.addEffect(new MobEffectInstance(effect));
        }
        potionCloud.setDuration(60);
        potionCloud.setRadius(1);
        potionCloud.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(potionCloud);
    }

    @Override
    public void onHitEntity(ToolType type, Entity attacker, Entity victim) {
        if (type == ToolType.BOW)
            return;
        if (victim instanceof LivingEntity livingEntity) {
            for (MobEffectInstance effect : effects()) {
                livingEntity.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    @Override
    public MobTraitSerializer<PotionTrait> serializer() {
        return SERIALIZER;
    }

    public static class Serializer implements MobTraitSerializer<PotionTrait> {
        public static final Codec<MobEffectInstance> MOB_EFFECT_INSTANCE_CODEC = RecordCodecBuilder
                .create(instance -> instance.group(
                        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect")
                                .forGetter(MobEffectInstance::getEffect),
                        Codec.INT.optionalFieldOf("duration", 200).forGetter(MobEffectInstance::getDuration),
                        Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
                        Codec.BOOL.optionalFieldOf("ambient", true).forGetter(MobEffectInstance::isAmbient),
                        Codec.BOOL.optionalFieldOf("visible", true).forGetter(MobEffectInstance::isVisible),
                        Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(MobEffectInstance::showIcon))
                        .apply(instance, MobEffectInstance::new));

        public static final MapCodec<PotionTrait> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MOB_EFFECT_INSTANCE_CODEC.listOf().fieldOf("effects").forGetter(PotionTrait::effects))
                .apply(instance, PotionTrait::new));

        @Override
        public Identifier id() {
            return Identifier.fromNamespaceAndPath(Spirit.MODID, "potion_effect");
        }

        @Override
        public MapCodec<PotionTrait> codec() {
            return CODEC;
        }
    }
}