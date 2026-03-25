package me.codexadrian.spirit.fabric;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * TODO: This class needs a complete rewrite for 1.21.11.
 * The old RenderType.CompositeState / ShaderStateShard / ShaderInstance system is gone.
 * The new system uses RenderPipeline and RenderSetup.
 * For now, this provides a fallback using the built-in entityTranslucent render type.
 */
public class FabricSoulShader {

    public static <T extends Entity> RenderType getSoulRenderType(T entity, Identifier texture) {
        // Fallback: use built-in translucent entity rendering
        return RenderTypes.entityTranslucent(texture);
    }
}
