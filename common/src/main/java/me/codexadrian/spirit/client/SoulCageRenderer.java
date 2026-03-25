package me.codexadrian.spirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoulCageRenderer implements BlockEntityRenderer<SoulCageBlockEntity, SoulCageRenderer.SoulCageRenderState> {

    public SoulCageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull SoulCageRenderState createRenderState() {
        return new SoulCageRenderState();
    }

    @Override
    public void extractRenderState(SoulCageBlockEntity blockEntity, SoulCageRenderState state, float partialTick,
            Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.hasEntity = blockEntity.hasLevel() && blockEntity.type != null;
        if (state.hasEntity) {
            Entity entity = blockEntity.getOrCreateEntity();
            state.entityBbWidth = entity.getBbWidth();
            state.entityBbHeight = entity.getBbHeight();
            state.spinDegrees = (float) blockEntity.getSpawner().getSpin();
            state.entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partialTick);
        }
    }

    @Override
    public void submit(SoulCageRenderState state, @NotNull PoseStack matrixStack,
            @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraState) {
        if (!state.hasEntity || state.entityRenderState == null)
            return;
        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.0D, 0.5D);

        float g = 0.53125F;
        float h = Math.max(state.entityBbWidth, state.entityBbHeight);
        if ((double) h > 1.0D) {
            g /= h;
        }

        matrixStack.translate(0.0D, 0.4000000059604645D, 0.0D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(state.spinDegrees));
        matrixStack.translate(0.0D, -0.20000000298023224D, 0.0D);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        matrixStack.scale(g, g, g);
        Minecraft.getInstance().getEntityRenderDispatcher().submit(
                state.entityRenderState, cameraState, 0.0D, 0.0D, 0.0D, matrixStack, collector);
        matrixStack.popPose();
    }

    public static class SoulCageRenderState extends BlockEntityRenderState {
        public boolean hasEntity = false;
        public float entityBbWidth;
        public float entityBbHeight;
        public float spinDegrees;
        public EntityRenderState entityRenderState;
    }
}
