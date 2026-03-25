package me.codexadrian.spirit.compat.rei.displays;

import me.codexadrian.spirit.compat.jei.ingredients.EntityIngredient;
import me.codexadrian.spirit.compat.rei.SpiritPlugin;
import me.codexadrian.spirit.compat.rei.categories.PedestalRecipeCategory;
import me.codexadrian.spirit.recipe.PedestalRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedestalDisplay extends BasicDisplay {
    private final PedestalRecipe recipe;

    public PedestalDisplay(RecipeHolder<PedestalRecipe> holder) {
        super(createEntityInput(holder.value()),
                List.of(createEntityOutput(holder.value()),
                        EntryIngredients.of(SpawnEggItem.byId(holder.value().entityOutput()))),
                Optional.ofNullable(holder.id()));
        this.recipe = holder.value();
    }

    public PedestalRecipe recipe() {
        return recipe;
    }

    private static EntryIngredient createEntityOutput(PedestalRecipe recipe) {
        var nbt = new CompoundTag();
        nbt.putBoolean("Corrupted", true);
        return EntryIngredient.of(EntryStack.of(SpiritPlugin.ENTITY_INGREDIENT,
                new EntityIngredient(recipe.entityOutput(), -45F, Optional.of(nbt))));
    }

    private static List<EntryIngredient> createEntityInput(PedestalRecipe recipe) {
        var nbt = new CompoundTag();
        nbt.putBoolean("Corrupted", true);
        List<EntryIngredient> list = recipe.entityInput().stream().filter(Holder::isBound).map(Holder::value)
                .map(type -> EntryIngredient.of(EntryStack.of(SpiritPlugin.ENTITY_INGREDIENT,
                        new EntityIngredient(type, 45F, Optional.of(nbt)))))
                .toList();
        List<EntryIngredient> finalList = new ArrayList<>(list);
        finalList.addAll(EntryIngredients.ofIngredients(recipe.ingredients()));
        recipe.activationItem().ifPresent(item -> finalList.add(EntryIngredients.ofIngredient(item)));
        return finalList;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return PedestalRecipeCategory.RECIPE;
    }
}
