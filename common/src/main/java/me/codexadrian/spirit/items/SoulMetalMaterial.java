package me.codexadrian.spirit.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class SoulMetalMaterial implements Tier {
    public static final SoulMetalMaterial INSTANCE = new SoulMetalMaterial();

    @Override
    public int getUses() {
        return 200;
    }

    @Override
    public float getSpeed() {
        return 9;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.5F;
    }

    public int getEnchantmentValue() {
        return 25;
    }

    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }
}