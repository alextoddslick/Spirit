package me.codexadrian.spirit.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.core.HolderLookup;

/**
 * Interface for data-driven "recipes" that use the recipe system as a data
 * loader. These don't actually craft anything - they just store data.
 * 
 * In 1.21, the Recipe API changed significantly. This interface now only
 * provides the common methods needed for serialization/registration.
 */
public interface SyncedData {

    RecipeSerializer<?> getSerializer();

    RecipeType<?> getType();

    default ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
}
