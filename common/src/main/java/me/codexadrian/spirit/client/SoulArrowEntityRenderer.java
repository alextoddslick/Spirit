package me.codexadrian.spirit.client;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.entity.SoulArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class SoulArrowEntityRenderer extends ArrowRenderer<SoulArrowEntity, ArrowRenderState> {

    public static Identifier SOUL_ARROW = Identifier.fromNamespaceAndPath(Spirit.MODID, "textures/entity/soul_arrow.png");

    public SoulArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return SoulArrowEntityRenderer.SOUL_ARROW;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    // NOTE: The old custom render() method with soul shader vertex drawing has been removed.
    // In 1.21.11, ArrowRenderer uses submit() with the state extraction pattern.
    // The custom soul shader rendering needs to be reimplemented via the new RenderType/RenderSetup system.
    // TODO: Reimplement custom soul arrow rendering if needed
}
