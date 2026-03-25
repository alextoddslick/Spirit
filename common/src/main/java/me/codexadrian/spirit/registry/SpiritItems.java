package me.codexadrian.spirit.registry;

import me.codexadrian.spirit.items.*;
import me.codexadrian.spirit.items.tools.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

import static me.codexadrian.spirit.Spirit.MODID;
import static me.codexadrian.spirit.Spirit.SPIRIT;
import static me.codexadrian.spirit.platform.fabric.Services.REGISTRY;

public class SpiritItems {
        public static final Supplier<Item> SOUL_CRYSTAL_SHARD = registerItem("soul_crystal_shard",
                        () -> new MobCrystalItem(itemProps("soul_crystal_shard").stacksTo(1)));

        public static final Supplier<Item> CRUDE_SOUL_CRYSTAL = registerItem("crude_soul_crystal",
                        () -> new CrudeSoulCrystalItem(itemProps("crude_soul_crystal").stacksTo(1)));

        public static final Supplier<Item> SOUL_CRYSTAL = registerItem("soul_crystal",
                        () -> new SoulCrystalItem(itemProps("soul_crystal").stacksTo(1).rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL = registerItem("soul_steel_ingot",
                        () -> new Item(itemProps("soul_steel_ingot")));

        public static final Supplier<Item> SOUL_POWDER = registerItem("soul_powder",
                        () -> new Item(itemProps("soul_powder")));

        public static final Supplier<Item> SOUL_STEEL_AXE = registerItem("soul_steel_axe",
                        () -> new SoulSteelAxe(itemProps("soul_steel_axe").rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_BOW = registerItem("soul_steel_bow",
                        () -> new SoulSteelBow(itemProps("soul_steel_bow").durability(64).rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_HOE = registerItem("soul_steel_hoe",
                        () -> new SoulSteelHoe(itemProps("soul_steel_hoe").rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_PICKAXE = registerItem("soul_steel_pickaxe",
                        () -> new SoulSteelPickaxe(itemProps("soul_steel_pickaxe").rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_SHOVEL = registerItem("soul_steel_shovel",
                        () -> new SoulSteelShovel(itemProps("soul_steel_shovel").rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_BLADE = registerItem("soul_steel_sword",
                        () -> new SoulSteelSword(itemProps("soul_steel_sword").rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_WAND = registerItem("soul_steel_wand",
                        () -> new SoulSteelWand(itemProps("soul_steel_wand").rarity(Rarity.RARE).stacksTo(1)));

        static Item.Properties itemProps(String name) {
                return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name)));
        }

        private static Supplier<Item> registerItem(String name, Supplier<Item> item) {
                var newItem = REGISTRY.registerItem(name, item);
                me.codexadrian.spirit.Spirit.TAB_ITEMS.add(newItem);
                return newItem;
        }

        public static void registerAll() {
        }
}
