package me.codexadrian.spirit.fabric.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * TODO: This mixin previously loaded a custom soul shader via ShaderInstance.
 * ShaderInstance has been completely removed in 1.21.11.
 * The rendering pipeline now uses RenderPipeline/RenderSetup.
 * The soul shader effect needs to be reimplemented using the new system.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // Stubbed out - ShaderInstance no longer exists in 1.21.11
}
