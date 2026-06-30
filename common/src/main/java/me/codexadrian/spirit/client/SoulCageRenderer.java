package me.codexadrian.spirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
            if (entity == null) {
                state.hasEntity = false;
            } else {
                state.entityBbWidth = entity.getBbWidth();
                state.entityBbHeight = entity.getBbHeight();
                state.spinDegrees = (float) blockEntity.getSpawner().getSpin();
                state.entityRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity,
                        partialTick);
                // The caged entity is never placed in the world (no position set, never added to the
                // level), so extractEntity computes lighting from its bogus default position and the
                // model renders pitch black. Use the cage block's own light level instead.
                state.entityRenderState.lightCoords = state.lightCoords;
            }
        }

        Level level = blockEntity.getLevel();
        // Stats are drawn as a HUD overlay (see CageStatsHud) rather than floating world text:
        // Font#drawInBatch produces no visible output inside this block-entity renderer in this build.
        if (level != null && shouldShowStats(blockEntity, level)) {
            CageStatsHud.submit(buildStatsLines(blockEntity, level));
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

    /** Show stats while this cage is pinned (wand toggle), or during a temporary wand inspect. */
    private boolean shouldShowStats(SoulCageBlockEntity be, Level level) {
        if (be.isEmpty()) {
            return false;
        }
        return be.pinned || level.getGameTime() < be.inspectUntil;
    }

    /** Builds the stat readout lines shown for a cage (rendered by {@link CageStatsHud}). */
    private List<Component> buildStatsLines(SoulCageBlockEntity be, Level level) {
        ItemStack crystal = be.getItem(0);
        int souls = SoulUtils.getSoulsInCrystal(crystal);
        Tier tier = SoulUtils.getTier(crystal, level);
        Tier next = SoulUtils.getNextTier(crystal, level);

        List<Component> lines = new ArrayList<>();
        Component title = be.type != null ? be.type.getDescription().copy() : crystal.getHoverName().copy();
        lines.add(title.copy().withStyle(ChatFormatting.WHITE));
        lines.add(Component.translatable("misc.spirit.tier",
                Component.translatable(tier == null ? SpiritConfig.getInitialTierName() : tier.displayName()))
                .withStyle(ChatFormatting.AQUA));
        lines.add((next != null
                ? Component.literal(souls + " / " + next.requiredSouls() + " souls")
                : Component.literal(souls + " souls")).withStyle(ChatFormatting.GRAY));
        if (tier != null) {
            int remaining = Math.max(0,
                    be.clientSpawnDelay - (int) (level.getGameTime() - be.clientSpawnDelaySyncTime));
            lines.add(Component.literal("Next spawn: ~" + (remaining / 20) + "s").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Spawns " + tier.spawnCount() + " · radius " + tier.spawnRange())
                    .withStyle(ChatFormatting.DARK_GRAY));
            String range = tier.nearbyRange() > 0 ? (tier.nearbyRange() + " blocks") : "unlimited";
            lines.add(Component.literal("Range " + range + (tier.redstoneControlled() ? " · redstone" : ""))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    public static class SoulCageRenderState extends BlockEntityRenderState {
        public boolean hasEntity = false;
        public float entityBbWidth;
        public float entityBbHeight;
        public float spinDegrees;
        public EntityRenderState entityRenderState;
    }
}
