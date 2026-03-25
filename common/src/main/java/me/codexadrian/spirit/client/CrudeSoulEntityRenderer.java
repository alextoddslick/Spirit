package me.codexadrian.spirit.client;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.entity.CrudeSoulEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrudeSoulEntityRenderer extends LivingEntityRenderer<CrudeSoulEntity, LivingEntityRenderState, CrudeSoulEntityModel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Spirit.MODID, "textures/entity/crude_soul.png");

    public CrudeSoulEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CrudeSoulEntityModel(context.bakeLayer(CrudeSoulEntityModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(@NotNull LivingEntityRenderState state, boolean bl, boolean bl2, boolean bl3) {
        // TODO: Soul shader rendering needs rework for 1.21.11 - ShaderInstance is removed
        // Previously used ClientServices.SHADERS.getSoulShader() which relied on custom ShaderInstance
        // For now, use default entity rendering as a fallback
        return super.getRenderType(state, bl, bl2, bl3);
    }

    @Override
    public @NotNull LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
