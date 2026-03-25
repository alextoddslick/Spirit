package me.codexadrian.spirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.codexadrian.spirit.blocks.blockentity.SoulPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoulPedestalRenderer implements BlockEntityRenderer<SoulPedestalBlockEntity, SoulPedestalRenderer.SoulPedestalRenderState> {

    public SoulPedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull SoulPedestalRenderState createRenderState() {
        return new SoulPedestalRenderState();
    }

    @Override
    public void extractRenderState(SoulPedestalBlockEntity blockEntity, SoulPedestalRenderState state,
            float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.hasLevel = blockEntity.hasLevel();
        state.hasEntity = blockEntity.type != null;
        state.age = blockEntity.age;
        state.partialTick = partialTick;
        if (state.hasLevel && state.hasEntity) {
            Entity entity = blockEntity.getOrCreateEntity();
            entity.tickCount = blockEntity.age;
            entity.tick();
            state.entityBbWidth = entity.getBbWidth();
            state.entityBbHeight = entity.getBbHeight();
            state.entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partialTick);
        }
    }

    @Override
    public void submit(SoulPedestalRenderState state, @NotNull PoseStack matrixStack,
            @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraState) {
        if (!state.hasLevel)
            return;
        if (state.hasEntity && state.entityRenderState != null) {
            float g = 0.75F;
            float h = Math.max(state.entityBbWidth, state.entityBbHeight);
            if ((double) h > 1.0D) {
                g /= h;
            }

            matrixStack.pushPose();
            matrixStack.translate(0.5D, .75D, 0.5D);
            var degrees = state.age;
            var oldTick = Math.max(state.age - 1, 0);
            matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, oldTick, degrees) % 360));
            matrixStack.scale(g, g, g);
            matrixStack.translate(0, Math.sin(state.age * .1) * 0.05 + 0.05, 0);
            Minecraft.getInstance().getEntityRenderDispatcher().submit(
                    state.entityRenderState, cameraState, 0.0D, 0.0D, 0.0D, matrixStack, collector);
            matrixStack.popPose();
        }
    }

    public static class SoulPedestalRenderState extends BlockEntityRenderState {
        public boolean hasLevel = false;
        public boolean hasEntity = false;
        public int age;
        public float partialTick;
        public float entityBbWidth;
        public float entityBbHeight;
        public EntityRenderState entityRenderState;
    }
}
