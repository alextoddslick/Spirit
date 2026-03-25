
package me.codexadrian.spirit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

public record MobTraitData(EntityType<?> entity, List<MobTrait<?>> traits)
        implements SyncedData, net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput> {

    public static com.mojang.serialization.MapCodec<MobTraitData> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(MobTraitData::entity),
                MobTraitRegistry.CODEC.listOf().fieldOf("traits").forGetter(MobTraitData::traits))
                .apply(instance, MobTraitData::new));
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>> getSerializer() {
        return SpiritMisc.MOB_TRAIT_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>> getType() {
        return SpiritMisc.MOB_TRAIT.get();
    }

    // Recipe Interface Implementation
    @Override
    public boolean matches(net.minecraft.world.item.crafting.RecipeInput input, net.minecraft.world.level.Level level) {
        return false;
    }

    @Override
    public net.minecraft.world.item.ItemStack assemble(net.minecraft.world.item.crafting.RecipeInput input,
            net.minecraft.core.HolderLookup.Provider registries) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    public static Optional<MobTraitData> getEffectForEntity(EntityType<?> entityType, RecipeManager manager) {
        // In 1.21, getAllRecipesFor signature changed - using getRecipes alternative
        return manager.getRecipes().stream()
                .filter(holder -> holder.value() instanceof MobTraitData)
                .map(holder -> (MobTraitData) holder.value())
                .filter(recipe -> recipe.entity().equals(entityType))
                .findFirst();
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<MobTraitData> getEffect(String id, RecipeManager manager) {
        return manager.byKey(ResourceKey.create(Registries.RECIPE, Identifier.tryParse(id))).map(holder -> (MobTraitData) holder.value());
    }
}
