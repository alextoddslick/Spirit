package me.codexadrian.spirit;

import me.codexadrian.spirit.client.*;
import me.codexadrian.spirit.platform.fabric.ClientServices;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class SpiritClient {

    public static void initClient() {
        ClientServices.CLIENT.setRenderLayer(SpiritBlocks.BROKEN_SPAWNER.get(), ChunkSectionLayer.CUTOUT);
        ClientServices.CLIENT.setRenderLayer(SpiritBlocks.SOUL_CAGE.get(), ChunkSectionLayer.CUTOUT);
        ClientServices.CLIENT.setRenderLayer(SpiritBlocks.SOUL_PEDESTAL.get(), ChunkSectionLayer.CUTOUT);
        ClientServices.CLIENT.setRenderLayer(SpiritBlocks.PEDESTAL.get(), ChunkSectionLayer.CUTOUT);
        ClientServices.CLIENT.setRenderLayer(SpiritBlocks.SOUL_GLASS.get(), ChunkSectionLayer.TRANSLUCENT);
        for (var glass : SpiritBlocks.SOUL_GLASS_BLOCKS) {
            ClientServices.CLIENT.setRenderLayer(glass.get(), ChunkSectionLayer.TRANSLUCENT);
        }
        ClientServices.CLIENT.registerBlockEntityRenderers(SpiritBlocks.SOUL_CAGE_ENTITY.get(), SoulCageRenderer::new);
        ClientServices.CLIENT.registerBlockEntityRenderers(SpiritBlocks.SOUL_PEDESTAL_ENTITY.get(),
                SoulPedestalRenderer::new);
        ClientServices.CLIENT.registerBlockEntityRenderers(SpiritBlocks.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
        ClientServices.CLIENT.registerEntityRenderer(SpiritMisc.SOUL_ARROW_ENTITY, SoulArrowEntityRenderer::new);
        ClientServices.CLIENT.registerEntityRenderer(SpiritMisc.SOUL_ENTITY, CrudeSoulEntityRenderer::new);

        // TODO: Item property registrations removed - ClampedItemPropertyFunction no longer exists in 1.21.11.
        // Item model properties (pull, pulling, filled) are now data-driven via item model definition JSON files.
        // Create item model definitions in assets/spirit/items/ for:
        //   - soul_bow (pull, pulling properties)
        //   - soul_crystal_shard (filled property)
    }

}
