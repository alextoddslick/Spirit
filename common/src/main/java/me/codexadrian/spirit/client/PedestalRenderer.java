package me.codexadrian.spirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.codexadrian.spirit.blocks.blockentity.PedestalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.PedestalRenderState> {
    private final ItemModelResolver itemModelResolver;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NotNull PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(PedestalBlockEntity blockEntity, PedestalRenderState state, float partialTick,
            Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.isEmpty = !blockEntity.hasLevel() || blockEntity.isEmpty();
        state.age = blockEntity.age;
        if (!state.isEmpty) {
            state.itemRenderState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(state.itemRenderState, blockEntity.getItem(0),
                    ItemDisplayContext.NONE, blockEntity.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(PedestalRenderState state, @NotNull PoseStack matrixStack,
            @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraState) {
        if (state.isEmpty)
            return;
        matrixStack.pushPose();
        matrixStack.translate(0.5D, 1.05D, 0.5D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(state.age % 360));
        matrixStack.scale(0.55f, 0.55f, 0.55f);
        matrixStack.translate(0, Math.sin(state.age * .1) * 0.05 + 0.05, 0);
        if (state.itemRenderState != null) {
            state.itemRenderState.submit(matrixStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        }
        matrixStack.popPose();
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public boolean isEmpty = true;
        public int age;
        public ItemStackRenderState itemRenderState;
    }
}
