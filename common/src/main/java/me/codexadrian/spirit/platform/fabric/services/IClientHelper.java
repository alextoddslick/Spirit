package me.codexadrian.spirit.platform.fabric.services;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IClientHelper {

    void registerItemProperty(Item pItem, ResourceLocation pName, ClampedItemPropertyFunction pProperty);

    <T extends BlockEntity> void registerBlockEntityRenderers(BlockEntityType<T> type, BlockEntityRendererProvider<T> provider);

    <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entity, EntityRendererProvider<T> rendererProvider);

    void setRenderLayer(Block block, RenderType type);

    <M extends AbstractContainerMenu> void registerMenuScreen(Supplier<MenuType<M>> type, MenuScreenFactory<M> factory);

    @FunctionalInterface
    interface MenuScreenFactory<M extends AbstractContainerMenu> {
        AbstractContainerScreen<M> create(M menu, Inventory inventory, Component title);
    }

}
