package me.codexadrian.spirit.items.tools;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.utils.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SoulSteelWand extends Item {
    public SoulSteelWand(Properties properties) {
        super(properties);
    }

    /**
     * Shift-right-click a soul cage to toggle its stats "pinned" on screen. This lives in
     * {@link #useOn} rather than the block's {@code useItemOn} because vanilla skips block
     * interaction when sneaking with a non-empty hand (the "sneak bypass"), so the block-side
     * handler is never reached while holding the wand — {@code useOn} still fires.
     *
     * <p>The pin flag is stored on the {@link SoulCageBlockEntity} (which syncs to clients), not on
     * the wand item, because held-item component changes were not reaching the renderer.
     */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            // Non-sneak cage interactions are handled by SoulCageBlock#useItemOn; let it run.
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockEntity(pos) instanceof SoulCageBlockEntity cage && !cage.isEmpty()) {
            if (!level.isClientSide) {
                cage.pinned = !cage.pinned;
                cage.update(Block.UPDATE_ALL);
                player.displayClientMessage(Component.literal(cage.pinned
                        ? "Pinned spawner stats (shift-right-click again to unpin)"
                        : "Unpinned spawner stats").withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        ClientUtils.shiftTooltip(list,
                List.of(Component.translatable("item.spirit.soul_steel_wand.desc").withStyle(ChatFormatting.GRAY)),
                List.of());
    }
}
