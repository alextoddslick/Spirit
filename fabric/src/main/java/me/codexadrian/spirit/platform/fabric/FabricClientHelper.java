package me.codexadrian.spirit.platform.fabric;

import me.codexadrian.spirit.platform.fabric.services.IClientHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class FabricClientHelper implements IClientHelper {

    @Override
    public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderers(BlockEntityType<T> type, BlockEntityRendererProvider<T, S> provider) {
        BlockEntityRendererRegistry.register(type, provider);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entity, EntityRendererProvider<T> rendererProvider) {
        EntityRendererRegistry.register(entity.get(), rendererProvider);
    }

    @Override
    public void setRenderLayer(Block block, ChunkSectionLayer layer) {
        BlockRenderLayerMap.putBlock(block, layer);
    }
}
