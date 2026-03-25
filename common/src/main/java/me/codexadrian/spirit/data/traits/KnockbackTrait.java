package me.codexadrian.spirit.data.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.MobTrait;
import me.codexadrian.spirit.data.MobTraitSerializer;
import me.codexadrian.spirit.entity.SoulArrowEntity;
import net.minecraft.resources.Identifier;

public record KnockbackTrait(int knockback) implements MobTrait<KnockbackTrait> {

    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public void initializeArrow(SoulArrowEntity soulArrow) {
        // setKnockback method may have changed in 1.21 - TODO: verify API
        // soulArrow.setKnockback(knockback);
    }

    @Override
    public MobTraitSerializer<KnockbackTrait> serializer() {
        return SERIALIZER;
    }

    private static class Serializer implements MobTraitSerializer<KnockbackTrait> {
        public static final MapCodec<KnockbackTrait> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("knockback").forGetter(KnockbackTrait::knockback))
                .apply(instance, KnockbackTrait::new));

        @Override
        public Identifier id() {
            return Identifier.fromNamespaceAndPath(Spirit.MODID, "knockback");
        }

        @Override
        public MapCodec<KnockbackTrait> codec() {
            return CODEC;
        }
    }
}
