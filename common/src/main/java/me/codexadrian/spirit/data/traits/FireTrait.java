package me.codexadrian.spirit.data.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.MobTrait;
import me.codexadrian.spirit.data.MobTraitSerializer;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.entity.SoulArrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FireTrait implements MobTrait<FireTrait> {
    private final int burnTime;

    public FireTrait(int burnTime) {
        this.burnTime = burnTime;
    }

    public int burnTime() {
        return burnTime;
    }

    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public void initializeArrow(SoulArrowEntity soulArrow) {
        // This method is now empty as its logic has been moved to onHitEntity
    }

    @Override
    public void onHitEntity(ToolType type, Entity attacker, Entity victim) {
        if (type == ToolType.BOW && attacker instanceof SoulArrowEntity soulArrow
                && victim instanceof LivingEntity livingVictim) {
            soulArrow.igniteForSeconds(burnTime());
            livingVictim.igniteForSeconds(burnTime());
        }
    }

    @Override
    public void onHitBlock(ToolType type, Entity entity, BlockState blockState, Level level, BlockPos pos) {
        if (BaseFireBlock.canBePlacedAt(level, pos.relative(Direction.UP), Direction.UP)) {
            level.setBlock(pos.relative(Direction.UP), BaseFireBlock.getState(level, pos.relative(Direction.UP)), 11);
        }
    }

    @Override
    public MobTraitSerializer<FireTrait> serializer() {
        return SERIALIZER;
    }

    private static class Serializer implements MobTraitSerializer<FireTrait> {
        public static final MapCodec<FireTrait> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("burnTime").orElse(120).forGetter(FireTrait::burnTime))
                .apply(instance, FireTrait::new));

        @Override
        public ResourceLocation id() {
            return ResourceLocation.fromNamespaceAndPath(Spirit.MODID, "fire");
        }

        @Override
        public MapCodec<FireTrait> codec() {
            return CODEC;
        }
    }
}
