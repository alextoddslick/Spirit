package me.codexadrian.spirit;

import me.codexadrian.spirit.network.NetworkHandler;
import me.codexadrian.spirit.platform.fabric.Services;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Spirit {

    public static final String MODID = "spirit";
    public static final String MOD_NAME = "Spirit";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new ArrayList<>();
    public static final Supplier<CreativeModeTab> SPIRIT = Services.REGISTRY.registerCreativeTab(
            Identifier.fromNamespaceAndPath(MODID, "itemgroup"),
            () -> new ItemStack(SpiritItems.SOUL_CRYSTAL.get()),
            (parameters, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())));

    public static final TagKey<EntityType<?>> BLACKLISTED_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MODID, "soul_cage_blacklisted"));
    public static final TagKey<EntityType<?>> REVITALIZER_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("vitalize", "revitalizer_blacklist"));
    public static final TagKey<EntityType<?>> COLLECT_BLACKLISTED_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MODID, "collect_blacklisted"));

    public static final TagKey<EntityType<?>> UNCOMMON = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Spirit.MODID, "rarity/uncommon"));
    public static final TagKey<EntityType<?>> RARE = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Spirit.MODID, "rarity/rare"));
    public static final TagKey<EntityType<?>> EPIC = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Spirit.MODID, "rarity/epic"));
    public static final TagKey<EntityType<?>> LEGENDARY = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Spirit.MODID, "rarity/legendary"));

    public static final TagKey<Item> SOUL_STEEL_MAINHAND = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "soul_steel_mainhand"));
    public static final TagKey<Item> SOUL_FIRE_IMMUNE = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "soul_fire_immune"));
    public static final TagKey<Item> SOUL_STEEL_OFFHAND = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "soul_steel_offhand"));

    public static final int SOUL_COLOR = 0xFF00fffb;

    public static void onInitialize() {
        SpiritMisc.registerAll();
        SpiritBlocks.registerAll();
        SpiritItems.registerAll();
        NetworkHandler.register();
    }
}
