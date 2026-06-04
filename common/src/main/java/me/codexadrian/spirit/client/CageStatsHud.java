package me.codexadrian.spirit.client;

import me.codexadrian.spirit.registry.SpiritItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws Soul Cage stats as a HUD overlay instead of floating world text. World-space text
 * ({@code Font#drawInBatch} inside the block-entity renderer) renders nothing in this build, while
 * GUI text works, so the cage renderer feeds its stat lines here each frame via {@link #submit} and
 * the loader's HUD hook calls {@link #render} to draw them.
 */
public final class CageStatsHud {

    private CageStatsHud() {
    }

    /** Stat blocks submitted by visible pinned/inspected cages this frame; drained by {@link #render}. */
    private static final List<List<Component>> PENDING = new ArrayList<>();

    /** Called from the cage renderer for each cage whose stats should currently show. */
    public static void submit(List<Component> lines) {
        PENDING.add(lines);
    }

    /** Called from the loader's HUD render hook; draws and clears whatever was submitted this frame. */
    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        // Only show the readout while the player is actually holding the Soul Steel Wand; otherwise
        // discard whatever the cage renderers submitted this frame.
        if (mc.player == null || !isHoldingWand(mc.player)) {
            PENDING.clear();
            return;
        }
        if (PENDING.isEmpty()) {
            return;
        }
        Font font = mc.font;
        int screenWidth = graphics.guiWidth();
        int y = 8;
        for (List<Component> block : PENDING) {
            for (Component line : block) {
                int x = (screenWidth - font.width(line)) / 2;
                graphics.drawString(font, line, x, y, 0xFFFFFFFF, true);
                y += font.lineHeight + 1;
            }
            y += 4;
        }
        PENDING.clear();
    }

    private static boolean isHoldingWand(Player player) {
        return player.getMainHandItem().is(SpiritItems.SOUL_STEEL_WAND.get())
                || player.getOffhandItem().is(SpiritItems.SOUL_STEEL_WAND.get());
    }
}
