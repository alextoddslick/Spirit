package me.codexadrian.spirit.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import me.codexadrian.spirit.Constants;
import me.codexadrian.spirit.Spirit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * This class was largely inspired by or taken from the Resourceful Bees
 * repository with
 * the expressed permission from one of their developers.
 * 
 * @author Team Resourceful
 */

public class CodecRecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R> {
    private static final Gson GSON = new GsonBuilder().create();
    private final MapCodec<R> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;
    private final RecipeType<R> recipeType;

    public CodecRecipeSerializer(RecipeType<R> recipeType, MapCodec<R> codec) {
        this.codec = codec;
        this.recipeType = recipeType;
        // Create a streamCodec that serializes via JSON string to avoid needing custom
        // binary serialization
        this.streamCodec = StreamCodec.of(
                (buf, recipe) -> {
                    // Encode recipe to JSON, then write the JSON string
                    var result = codec.codec()
                            .encodeStart(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), recipe);
                    String jsonString = result.resultOrPartial(Spirit.LOGGER::error)
                            .map(json -> GSON.toJson(json))
                            .orElse("{}");
                    ByteBufCodecs.STRING_UTF8.encode(buf, jsonString);
                },
                (buf) -> {
                    // Read JSON string, then decode to recipe
                    String jsonString = ByteBufCodecs.STRING_UTF8.decode(buf);
                    JsonObject json = GSON.fromJson(jsonString, JsonObject.class);
                    return codec.codec().parse(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), json)
                            .resultOrPartial(e -> Spirit.LOGGER.error("CodecRecipeSerializer Decode Error: " + e))
                            .orElseThrow(() -> new RuntimeException("Failed to decode recipe from network"));
                });
    }

    @Override
    public MapCodec<R> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return this.streamCodec;
    }

    public RecipeType<R> type() {
        return recipeType;
    }
}