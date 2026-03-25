package me.codexadrian.spirit.platform.fabric;

import me.codexadrian.spirit.platform.fabric.services.IShaderHelper;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class FabricShaderHelper implements IShaderHelper {

    @Override
    public <T extends Entity> RenderType getSoulShader(T entity, Identifier texture) {
        // TODO: Custom soul shader needs reimplementation for 1.21.11.
        // ShaderInstance and RenderStateShard.ShaderStateShard no longer exist.
        // The rendering pipeline now uses RenderPipeline/RenderSetup.
        // For now, return a translucent entity render type as fallback.
        return RenderTypes.entityTranslucent(texture);
    }
}
