package me.codexadrian.spirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoulCageRenderer implements BlockEntityRenderer<SoulCageBlockEntity> {

    public SoulCageRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(SoulCageBlockEntity blockEntity, float f, @NotNull PoseStack matrixStack,
            @NotNull MultiBufferSource multiBufferSource, int i, int j) {
        if (!blockEntity.hasLevel())
            return;

        Level level = blockEntity.getLevel();
        if (level != null && !blockEntity.isEmpty() && level.getGameTime() < blockEntity.inspectUntil) {
            renderStats(blockEntity, level, matrixStack, multiBufferSource, i);
        }

        if (blockEntity.type == null)
            return;
        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.0D, 0.5D);
        var entity = blockEntity.getOrCreateEntity();

        float g = 0.53125F;
        float h = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if ((double) h > 1.0D) {
            g /= h;
        }

        matrixStack.translate(0.0D, 0.4000000059604645D, 0.0D);
        matrixStack.mulPose(Axis.YP.rotationDegrees((float) blockEntity.getSpawner().getSpin()));
        matrixStack.translate(0.0D, -0.20000000298023224D, 0.0D);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        matrixStack.scale(g, g, g);
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, f, matrixStack,
                multiBufferSource, i);
        matrixStack.popPose();
    }

    private void renderStats(SoulCageBlockEntity be, Level level, PoseStack pose,
            MultiBufferSource buffers, int packedLight) {
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

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        pose.pushPose();
        pose.translate(0.5D, 1.6D, 0.5D);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();
        int bg = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        float y = -(lines.size() * 10) / 2.0F;
        for (Component line : lines) {
            float x = -font.width(line) / 2.0F;
            font.drawInBatch(line, x, y, 0xFFFFFFFF, false, matrix, buffers,
                    Font.DisplayMode.NORMAL, bg, packedLight);
            y += 10.0F;
        }
        pose.popPose();
    }
}
