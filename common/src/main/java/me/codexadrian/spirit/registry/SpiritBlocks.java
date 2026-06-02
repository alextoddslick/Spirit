package me.codexadrian.spirit.registry;

import me.codexadrian.spirit.blocks.*;
import me.codexadrian.spirit.blocks.blockentity.PedestalBlockEntity;
import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.blocks.blockentity.SoulPedestalBlockEntity;
import me.codexadrian.spirit.items.ChippedBlockItem;
import me.codexadrian.spirit.platform.fabric.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.codexadrian.spirit.Spirit.MODID;
import static me.codexadrian.spirit.Spirit.SPIRIT;
import static me.codexadrian.spirit.platform.fabric.Services.REGISTRY;

public class SpiritBlocks {
        public static final ArrayList<Supplier<Block>> SOUL_GLASS_BLOCKS = new ArrayList<>();

        public static final Supplier<Block> SOUL_CAGE = registerBlockWithItem("soul_cage", () -> new SoulCageBlock(
                        blockProps("soul_cage").strength(5.0F).sound(SoundType.METAL).noOcclusion()
                                        .requiresCorrectToolForDrops()));

        public static final Supplier<BlockEntityType<SoulCageBlockEntity>> SOUL_CAGE_ENTITY = REGISTRY
                        .registerBlockEntity("soul_cage", () -> Services.REGISTRY
                                        .createBlockEntityType(SoulCageBlockEntity::new, SOUL_CAGE.get()));

        public static final Supplier<Block> SOUL_PEDESTAL = registerBlockWithItem("soul_pedestal",
                        () -> new SoulPedestalBlock(
                                        blockProps("soul_pedestal").strength(5.0F).sound(SoundType.METAL)
                                                        .noOcclusion()
                                                        .requiresCorrectToolForDrops()));

        public static final Supplier<Block> CRYSTAL_PEDESTAL = registerBlockWithItem("crystal_pedestal",
                        () -> new CrystalPedestalBlock(
                                        blockProps("crystal_pedestal").strength(5.0F).sound(SoundType.METAL)
                                                        .noOcclusion()
                                                        .requiresCorrectToolForDrops()));

        public static final Supplier<BlockEntityType<SoulPedestalBlockEntity>> SOUL_PEDESTAL_ENTITY = REGISTRY
                        .registerBlockEntity("soul_pedestal", () -> Services.REGISTRY
                                        .createBlockEntityType(SoulPedestalBlockEntity::new, SOUL_PEDESTAL.get()));

        public static final Supplier<Block> PEDESTAL = registerBlockWithItem("pedestal", () -> new PedestalBlock(
                        blockProps("pedestal").strength(5.0F).sound(SoundType.METAL).noOcclusion()
                                        .requiresCorrectToolForDrops()));

        public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY = REGISTRY
                        .registerBlockEntity("pedestal",
                                        () -> Services.REGISTRY.createBlockEntityType(PedestalBlockEntity::new,
                                                        PEDESTAL.get(), CRYSTAL_PEDESTAL.get()));

        public static final Supplier<Block> SOUL_GLASS = registerBlockWithItem("soul_glass",
                        () -> new Block(blockProps("soul_glass").strength(0.3F).sound(SoundType.GLASS)
                                        .noOcclusion()
                                        .isValidSpawn((state, world, pos, entityType) -> false)
                                        .isRedstoneConductor((state, world, pos) -> false)
                                        .isSuffocating((state, world, pos) -> false)
                                        .isViewBlocking((state, world, pos) -> false)));

        public static final Supplier<Block> SOUL_SLATE = registerBlockWithItem("soul_slate",
                        () -> new Block(blockProps("soul_slate").strength(3.0F, 6.0F)
                                        .sound(SoundType.DEEPSLATE)));

        public static final Supplier<Block> SOUL_STEEL_BLOCK = registerBlockWithItem("soul_steel_block",
                        () -> new Block(blockProps("soul_steel_block").strength(5.0F, 6.0F).sound(SoundType.METAL)),
                        new Item.Properties().rarity(Rarity.RARE));

        public static final Supplier<Block> SOUL_POWDER_BLOCK = registerBlockWithItem("soul_powder_block",
                        () -> new Block(blockProps("soul_powder_block").strength(0.5F).sound(SoundType.SAND)),
                        new Item.Properties());

        public static final Supplier<Block> COMPRESSED_SOUL_POWDER_BLOCK = registerBlockWithItem(
                        "compressed_soul_powder_block",
                        () -> new Block(blockProps("compressed_soul_powder_block").strength(0.5F).sound(SoundType.SAND)),
                        new Item.Properties());

        public static final Supplier<Block> COMPRESSED_SOUL_SAND = registerBlockWithItem("compressed_soul_sand",
                        () -> new Block(blockProps("compressed_soul_sand").strength(0.5F).sound(SoundType.SAND)),
                        new Item.Properties());

        public static final Supplier<Block> BROKEN_SPAWNER = registerBlockWithItem("broken_spawner",
                        () -> new Block(blockProps("broken_spawner").strength(5.0F).sound(SoundType.METAL)
                                        .noOcclusion()
                                        .isValidSpawn((state, world, pos, entityType) -> false)
                                        .isRedstoneConductor((state, world, pos) -> false)
                                        .isSuffocating((state, world, pos) -> false)
                                        .isViewBlocking((state, world, pos) -> false)));

        public static final Supplier<Block> SOUL_OBSIDIAN = registerBlockWithItem("soul_obsidian",
                        () -> new Block(blockProps("soul_obsidian").strength(50.0F, 1200.0F)
                                        .sound(SoundType.STONE)));

        public static final Supplier<Block> SOUL_GLASS_PANE = registerBlockWithItem("soul_glass_pane",
                        () -> new SoulGlassPaneBlock(blockProps("soul_glass_pane").strength(0.3F)
                                        .sound(SoundType.GLASS).noOcclusion()));

        private static ResourceKey<Block> blockKey(String name) {
                return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MODID, name));
        }

        private static BlockBehaviour.Properties blockProps(String name) {
                return BlockBehaviour.Properties.of().setId(blockKey(name));
        }

        private static ResourceKey<Item> itemKey(String name) {
                return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name));
        }

        private static Supplier<Block> registerBlockWithItem(String name, Supplier<Block> block,
                        Item.Properties properties) {
                var newBlock = REGISTRY.registerBlock(name, block);
                var item = REGISTRY.registerItem(name, () -> new BlockItem(newBlock.get(), properties.setId(itemKey(name))));
                me.codexadrian.spirit.Spirit.TAB_ITEMS.add(item);
                return newBlock;
        }

        private static Supplier<Block> registerBlockWithItem(String name, Supplier<Block> block) {
                return registerBlockWithItem(name, block, new Item.Properties());
        }

        private static void registerChippedVariants(String name, int blocks) {
                for (int i = 1; i <= blocks; i++) {
                        final String blockName = name + "_" + i;
                        Supplier<Block> ctmBlock = REGISTRY.registerBlock(blockName,
                                        () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(blockName))));
                        var item = REGISTRY.registerItem(blockName,
                                        () -> new ChippedBlockItem(ctmBlock.get(),
                                                        new Item.Properties().setId(itemKey(blockName))));
                        me.codexadrian.spirit.Spirit.TAB_ITEMS.add(item);
                        SOUL_GLASS_BLOCKS.add(ctmBlock);
                }
        }

        public static void registerAll() {
                registerChippedVariants("soul_glass", 13);
        }
}
