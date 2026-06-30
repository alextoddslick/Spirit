package me.codexadrian.spirit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.core.HolderLookup;

public record Tier(
        String displayName,
        int requiredSouls,
        int minSpawnDelay,
        int maxSpawnDelay,
        int spawnCount,
        int spawnRange,
        int nearbyRange,
        boolean redstoneControlled,
        boolean ignoreSpawnConditions,
        Set<String> blacklist) implements SyncedData, net.minecraft.world.item.crafting.Recipe<RecipeInput> {

    public static com.mojang.serialization.MapCodec<Tier> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("displayName").forGetter(Tier::displayName),
                Codec.INT.fieldOf("requiredSouls").forGetter(Tier::requiredSouls),
                Codec.INT.fieldOf("minSpawnDelay").forGetter(Tier::minSpawnDelay),
                Codec.INT.fieldOf("maxSpawnDelay").forGetter(Tier::maxSpawnDelay),
                Codec.INT.fieldOf("spawnCount").forGetter(Tier::spawnCount),
                Codec.INT.fieldOf("spawnRange").forGetter(Tier::spawnRange),
                Codec.INT.fieldOf("nearbyRange").forGetter(Tier::nearbyRange),
                Codec.BOOL.fieldOf("redstoneControlled").orElse(false).forGetter(Tier::redstoneControlled),
                Codec.BOOL.fieldOf("ignoreSpawnConditions").orElse(false).forGetter(Tier::ignoreSpawnConditions),
                createSetCodec(Codec.STRING).orElse(new HashSet<>()).fieldOf("blacklist").forGetter(Tier::blacklist))
                .apply(instance, Tier::new));
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<RecipeInput>> getSerializer() {
        return SpiritMisc.TIER_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends net.minecraft.world.item.crafting.Recipe<RecipeInput>> getType() {
        return SpiritMisc.TIER_RECIPE.get();
    }

    // Recipe interface implementation for 1.21.11
    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    @Nullable
    public static Tier getTier(int souls, String type, Level level, boolean getNextTier) {
        Tier storedTier = null;
        List<Tier> tiers = new ArrayList<>(getTiers(level));
        if (tiers.isEmpty())
            return null;

        tiers.sort(Comparator.comparingInt(value -> -value.requiredSouls()));
        if (!getNextTier && souls < tiers.get(tiers.size() - 1).requiredSouls())
            return null;

        for (Tier tier : tiers) {
            if (type == null || !tier.blacklist().contains(type)) {
                if (souls < tier.requiredSouls())
                    storedTier = tier;
                else if (!getNextTier) {
                    storedTier = tier;
                    break;
                } else
                    break;
            }
        }
        return storedTier;
    }

    public static Tier getHighestTier(String type, Level level) {
        Tier storedTier = null;
        for (Tier tier : getTiers(level)) {
            if (type == null || !tier.blacklist().contains(type)) {
                if (storedTier == null || storedTier.requiredSouls() < tier.requiredSouls()) {
                    storedTier = tier;
                }
            }
        }
        return storedTier;
    }

    public static Tier getTier(int souls, String type, Level level) {
        return getTier(souls, type, level, false);
    }

    public static List<Tier> getTiers(Level level) {
        // recipeAccess() only returns the full RecipeManager on the server; on the client it's a
        // ClientRecipeContainer (curated recipe-book subsets only), which can't enumerate Tier "recipes".
        // Tier data isn't synced to clients yet, so client-side callers (e.g. item bar rendering) get
        // nothing here rather than crashing.
        if (!(level.recipeAccess() instanceof net.minecraft.world.item.crafting.RecipeManager recipeManager)) {
            return List.of();
        }

        return recipeManager.getRecipes().stream()
                .filter(holder -> holder.value().getType() == SpiritMisc.TIER_RECIPE.get())
                .map(holder -> (Tier) holder.value())
                .toList();
    }

    private static <A> Codec<Set<A>> createSetCodec(Codec<A> codec) {
        return codec.listOf().xmap(HashSet::new, ArrayList::new);
    }
}