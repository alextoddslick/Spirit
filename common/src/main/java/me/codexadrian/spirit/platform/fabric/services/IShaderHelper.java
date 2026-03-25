package me.codexadrian.spirit.platform.fabric.services;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public interface IShaderHelper {

    // TODO: setSoulShader removed - ShaderInstance no longer exists in 1.21.11
    // The shader system has been completely overhauled. Need to use RenderPipeline/RenderSetup instead.

    <T extends Entity> RenderType getSoulShader(T entity, Identifier texture);
}
