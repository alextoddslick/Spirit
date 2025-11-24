package me.codexadrian.spirit.registry;

import me.codexadrian.spirit.items.*;
import me.codexadrian.spirit.items.tools.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

import static me.codexadrian.spirit.Spirit.SPIRIT;
import static me.codexadrian.spirit.platform.fabric.Services.REGISTRY;

public class SpiritItems {
        public static final Supplier<Item> SOUL_CRYSTAL_SHARD = registerItem("soul_crystal_shard",
                        () -> new MobCrystalItem(new Item.Properties().stacksTo(1)));

        public static final Supplier<Item> CRUDE_SOUL_CRYSTAL = registerItem("crude_soul_crystal",
                        () -> new CrudeSoulCrystalItem(new Item.Properties().stacksTo(1)));

        public static final Supplier<Item> SOUL_CRYSTAL = registerItem("soul_crystal",
                        () -> new SoulCrystalItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL = registerItem("soul_steel_ingot",
                        () -> new Item(new Item.Properties()));

        public static final Supplier<Item> SOUL_POWDER = registerItem("soul_powder",
                        () -> new Item(new Item.Properties()));

        public static final Supplier<Item> SOUL_STEEL_AXE = registerItem("soul_steel_axe",
                        () -> new SoulSteelAxe(new Item.Properties().rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_BOW = registerItem("soul_steel_bow",
                        () -> new SoulSteelBow(new Item.Properties().durability(64).rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_HOE = registerItem("soul_steel_hoe",
                        () -> new SoulSteelHoe(new Item.Properties().rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_PICKAXE = registerItem("soul_steel_pickaxe",
                        () -> new SoulSteelPickaxe(new Item.Properties().rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_SHOVEL = registerItem("soul_steel_shovel",
                        () -> new SoulSteelShovel(new Item.Properties().rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_BLADE = registerItem("soul_steel_sword",
                        () -> new SoulSteelSword(new Item.Properties().rarity(Rarity.RARE)));

        public static final Supplier<Item> SOUL_STEEL_WAND = registerItem("soul_steel_wand",
                        () -> new SoulSteelWand(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

        private static Supplier<Item> registerItem(String name, Supplier<Item> item) {
                var newItem = REGISTRY.registerItem(name, item);
                me.codexadrian.spirit.Spirit.TAB_ITEMS.add(newItem);
                return newItem;
        }

        public static void registerAll() {
        }
}
