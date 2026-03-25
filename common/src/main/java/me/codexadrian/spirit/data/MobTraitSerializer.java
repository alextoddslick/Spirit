package me.codexadrian.spirit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

/**
 * This class was largely inspired by or taken from the Resourceful Bees
 * repository with
 * the expressed permission from one of their developers.
 * 
 * @author Team Resourceful
 */

public interface MobTraitSerializer<T extends MobTrait<T>> {
    Identifier id();

    MapCodec<T> codec();
}
