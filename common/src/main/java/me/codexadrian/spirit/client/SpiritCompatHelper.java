package me.codexadrian.spirit.client;

import me.codexadrian.spirit.Corrupted;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class SpiritCompatHelper {
    public static RenderType getRenderType(Entity livingEntity, Identifier texture, RenderType renderType) {
        if (livingEntity instanceof Corrupted corrupted && corrupted.isCorrupted()) {
            // TODO: Soul shader rendering needs rework for 1.21.11 - ShaderInstance is removed.
            // Previously used ClientServices.SHADERS.getSoulShader() which relied on custom ShaderInstance.
            // For now, return the default renderType as a fallback.
            return renderType;
        }
        return renderType;
    }
}
