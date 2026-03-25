package me.codexadrian.spirit.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class SoulMetalMaterial {
    public static final TagKey<Item> REPAIR_ITEMS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("spirit", "repairs_soul_steel"));

    public static final ToolMaterial INSTANCE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            200,    // durability
            9.0F,   // speed
            3.5F,   // attackDamageBonus
            25,     // enchantmentValue
            REPAIR_ITEMS
    );
}
