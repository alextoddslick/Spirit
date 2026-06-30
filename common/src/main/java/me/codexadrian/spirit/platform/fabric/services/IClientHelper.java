package me.codexadrian.spirit.platform.fabric.services;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IClientHelper {

    // TODO: Item properties system completely changed in 1.21.11 - ClampedItemPropertyFunction removed.
    // Item model properties (pull, pulling, filled) are now data-driven via item model definition JSON files.

    <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderers(BlockEntityType<T> type, BlockEntityRendererProvider<T, S> provider);

    <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entity, EntityRendererProvider<T> rendererProvider);

    void setRenderLayer(Block block, ChunkSectionLayer layer);

    <M extends AbstractContainerMenu> void registerMenuScreen(Supplier<MenuType<M>> type, MenuScreenFactory<M> factory);

    @FunctionalInterface
    interface MenuScreenFactory<M extends AbstractContainerMenu> {
        AbstractContainerScreen<M> create(M menu, Inventory inventory, Component title);
    }

}
