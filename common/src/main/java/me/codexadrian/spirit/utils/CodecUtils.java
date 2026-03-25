package me.codexadrian.spirit.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Ingredient;

public class CodecUtils {
    public static final Codec<Ingredient> INGREDIENT_CODEC = Codec.PASSTHROUGH.comapFlatMap(CodecUtils::readIngredient,
            ingredient -> CodecUtils.writeIngredient(ingredient, JsonOps.INSTANCE));

    public static DataResult<Ingredient> readIngredient(Dynamic<?> dynamic) {
        return Ingredient.CODEC_NONEMPTY.parse(dynamic);
    }

    public static <T> Dynamic<T> writeIngredient(Ingredient ingredient, DynamicOps<T> ops) {
        return new Dynamic<>(ops, Ingredient.CODEC_NONEMPTY.encodeStart(ops, ingredient).getOrThrow());
    }
}
