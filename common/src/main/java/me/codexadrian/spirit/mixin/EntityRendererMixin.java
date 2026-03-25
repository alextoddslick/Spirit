package me.codexadrian.spirit.mixin;

import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.platform.fabric.ClientServices;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        implements RenderLayerParent<S, M> {

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void getRenderType(S state, boolean bl, boolean bl2, boolean bl3,
            CallbackInfoReturnable<RenderType> cir) {
        // TODO: The corrupted soul shader override needs rework for 1.21.11.
        // In the new state-based system, we no longer have direct access to the entity in getRenderType.
        // The entity data needs to be extracted into the render state during extractRenderState.
        // For now, this mixin is a no-op stub until the soul shader system is reimplemented.
    }
}
