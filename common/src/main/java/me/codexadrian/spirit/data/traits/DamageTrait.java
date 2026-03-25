package me.codexadrian.spirit.data.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.MobTrait;
import me.codexadrian.spirit.data.MobTraitSerializer;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.entity.SoulArrowEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record DamageTrait(float additionalDamage) implements MobTrait<DamageTrait> {

    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public void initializeArrow(SoulArrowEntity soulArrow) {
        // In 1.21.11, getBaseDamage() is removed. Use setBaseDamageFromMob to add damage.
        // Default arrow base damage is 2.0, so we set it to default + additional
        soulArrow.setBaseDamage(2.0 + additionalDamage());
    }

    @Override
    public MobTraitSerializer<DamageTrait> serializer() {
        return SERIALIZER;
    }

    private static class Serializer implements MobTraitSerializer<DamageTrait> {
        public static final MapCodec<DamageTrait> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("additionalDamage").forGetter(DamageTrait::additionalDamage))
                .apply(instance, DamageTrait::new));

        @Override
        public Identifier id() {
            return Identifier.fromNamespaceAndPath(Spirit.MODID, "damage");
        }

        @Override
        public MapCodec<DamageTrait> codec() {
            return CODEC;
        }
    }
}
