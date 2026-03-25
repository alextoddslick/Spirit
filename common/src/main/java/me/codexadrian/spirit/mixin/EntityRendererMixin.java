package me.codexadrian.spirit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.platform.fabric.ClientServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
        implements RenderLayerParent<T, M> {

    private LivingEntity currentlyRendered;

    @Final
    @Shadow
    protected M model;

    @Shadow
    protected abstract boolean isShaking(T livingEntity);

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    public void render(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int i, CallbackInfo ci) {
        if (((Corrupted) livingEntity).isCorrupted()) {
            currentlyRendered = livingEntity;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void postRender(LivingEntity livingEntity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (currentlyRendered != null) {
            currentlyRendered = null;
        }
    }

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void getRenderType(LivingEntity livingEntity, boolean bl, boolean bl2, boolean bl3,
            CallbackInfoReturnable<RenderType> cir) {
        if (((Corrupted) livingEntity).isCorrupted()) {
            cir.setReturnValue(ClientServices.SHADERS.getSoulShader(livingEntity,
                    ((LivingEntityRenderer) (Object) this).getTextureLocation(livingEntity)));
        }
    }
}
