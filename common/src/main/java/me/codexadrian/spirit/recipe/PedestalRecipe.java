package me.codexadrian.spirit.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.SyncedData;
import me.codexadrian.spirit.data.TagAndListSetCodec;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.registry.SpiritMisc;
import me.codexadrian.spirit.utils.CodecUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public record PedestalRecipe(HolderSet<EntityType<?>> entityInput,
        Optional<Ingredient> activationItem, boolean consumesActivator, List<Ingredient> ingredients,
        EntityType<?> entityOutput, int duration,
        Optional<CompoundTag> outputNbt)
        implements SyncedData, net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput> {

    public static Optional<PedestalRecipe> getEffect(String id, RecipeManager manager) {
        return manager.byKey(ResourceKey.create(Registries.RECIPE, Identifier.tryParse(id)))
                .map(holder -> holder.value())
                .filter(recipe -> recipe instanceof PedestalRecipe)
                .map(recipe -> (PedestalRecipe) recipe);
    }

    public static final MapCodec<PedestalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().listOf()
                    .fieldOf("entityInput").forGetter(r -> r.entityInput.stream().toList()),
            Ingredient.CODEC.optionalFieldOf("activationItem").forGetter(PedestalRecipe::activationItem),
            Codec.BOOL.fieldOf("consumesActivator").forGetter(PedestalRecipe::consumesActivator),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(PedestalRecipe::ingredients),
            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityOutput")
                    .forGetter(PedestalRecipe::entityOutput),
            Codec.INT.fieldOf("duration").forGetter(PedestalRecipe::duration),
            CompoundTag.CODEC.optionalFieldOf("outputNbt").forGetter(PedestalRecipe::outputNbt))
            .apply(instance,
                    (entityInput, activationItem, consumesActivator, ingredients, entityOutput, duration,
                            outputNbt) -> new PedestalRecipe(HolderSet.direct(entityInput), activationItem,
                                    consumesActivator, ingredients, entityOutput, duration, outputNbt)));

    @Override
    public RecipeType<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>> getType() {
        return SpiritMisc.SOUL_TRANSMUTATION_RECIPE.get();
    }

    public MapCodec<PedestalRecipe> codec() {
        return CODEC;
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>> getSerializer() {
        return SpiritMisc.SOUL_TRANSMUTATION_SERIALIZER.get();
    }

    // Recipe Interface Implementation
    @Override
    public boolean matches(net.minecraft.world.item.crafting.RecipeInput input, net.minecraft.world.level.Level level) {
        return false; // Handled manually
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.RecipeInput input,
            net.minecraft.core.HolderLookup.Provider registries) {
        return ItemStack.EMPTY; // Handled manually
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    public static List<PedestalRecipe> getRecipesForEntity(EntityType<?> entity, ItemStack stack,
            RecipeManager manager) {
        // In 1.21, getAllRecipesFor signature changed - using getRecipes alternative
        return manager.getRecipes().stream()
                .filter(holder -> holder.value() instanceof PedestalRecipe)
                .map(holder -> (PedestalRecipe) holder.value())
                .filter(recipe -> {
                    boolean stackMatches;
                    if (recipe.activationItem().isPresent()) {
                        stackMatches = recipe.activationItem().get().test(stack);
                    } else {
                        stackMatches = true;
                    }
                    return stackMatches && recipe.entityInput().stream()
                            .anyMatch(h -> h.value().equals(entity));
                }).toList();
    }
}
