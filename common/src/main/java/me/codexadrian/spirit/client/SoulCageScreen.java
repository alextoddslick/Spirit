package me.codexadrian.spirit.client;

import me.codexadrian.spirit.blocks.blockentity.SoulCageBlockEntity;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.menu.SoulCageMenu;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for the Soul Cage upgrade menu, backed by the soul_cage.png texture. The header strip shows live
 * stats plus the materials invested, the upgrade panel holds two dual slots (soul steel block/ingot for
 * range, netherite block/ingot for spawn time), the lower panel holds the player inventory, and a "Reset"
 * hot-spot below the slots wipes the upgrades back to tier defaults. Hovering a slot shows what it does.
 */
public class SoulCageScreen extends AbstractContainerScreen<SoulCageMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("spirit", "textures/gui/soul_cage.png");

    // Upgrade slot top-left corners (must match SoulCageMenu): soul steel, netherite.
    private static final int[] SLOT_X = {65, 103};
    private static final int UPGRADE_SLOT_Y = 65;

    // Faded placeholder shown in each empty upgrade slot so it's clear what each box accepts.
    private final ItemStack[] slotGhosts;

    // Control row below the slots: Reset icon (far left), Confirm (center), +/- mode toggle (far right).
    private static final int RESET_X = 6;
    private static final int RESET_Y = 84;
    private static final int RESET_W = 14;
    private static final int RESET_H = 14;
    private static final int CONFIRM_W = 50;
    private static final int CONFIRM_H = 14;
    private static final int CONFIRM_X = (184 - CONFIRM_W) / 2;
    private static final int CONFIRM_Y = 84;
    private static final int TOGGLE_X = 164;
    private static final int TOGGLE_Y = 84;
    private static final int TOGGLE_W = 14;
    private static final int TOGGLE_H = 14;

    /** Client-side toggle: true = Confirm adds upgrades (+), false = Confirm removes & refunds (-). */
    private boolean addMode = true;

    public SoulCageScreen(SoulCageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 184;
        this.imageHeight = 222;
        // Order matches the upgrade slot indices in SoulCageMenu (soul steel, netherite); show the block form.
        this.slotGhosts = new ItemStack[] {
                new ItemStack(SpiritBlocks.SOUL_STEEL_BLOCK.get()),
                new ItemStack(Items.NETHERITE_BLOCK),
        };
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth,
                imageHeight);
        // The new texture draws no slots, so paint backgrounds for everything.
        for (int i = 0; i < SLOT_X.length; i++) {
            int x = leftPos + SLOT_X[i];
            drawSlot(graphics, x, topPos + UPGRADE_SLOT_Y);
            // Faded ghost of the accepted item while the slot is empty.
            if (!menu.slots.get(i).hasItem()) {
                drawGhost(graphics, slotGhosts[i], x, topPos + UPGRADE_SLOT_Y);
            }
        }
        // Player inventory + hotbar backgrounds.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, leftPos + 12 + col * 18, topPos + 120 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, leftPos + 12 + col * 18, topPos + 180);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF1B1B1B);
        graphics.fill(x, y, x + 16, y + 16, 0x66000000);
    }

    /** Renders a dimmed "ghost" of the accepted item so an empty slot shows what goes in it. */
    private void drawGhost(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        // Dim quad over the item; drawn after renderItem so it paints on top.
        graphics.fill(x, y, x + 16, y + 16, 0xAA161616);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        // Button hints.
        if (isOver(x, y, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H)) {
            graphics.setTooltipForNextFrame(font, Component.literal(addMode
                    ? "Apply the items in the slots" : "Lower spawn range  (+1s spawn delay)"), x, y);
            return;
        }
        if (isOver(x, y, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H)) {
            graphics.setComponentTooltipForNextFrame(font, List.of(
                    Component.literal("Mode: " + (addMode ? "Increase range (+)" : "Decrease range (-)"))
                            .withStyle(addMode ? ChatFormatting.GREEN : ChatFormatting.RED),
                    Component.literal("Lowering range adds 1s to spawn delay").withStyle(ChatFormatting.DARK_GRAY),
                    Component.literal("Click to toggle").withStyle(ChatFormatting.DARK_GRAY)), x, y);
            return;
        }
        if (isOver(x, y, RESET_X, RESET_Y, RESET_W, RESET_H)) {
            graphics.setTooltipForNextFrame(font, Component.literal("Reset upgrades (no refund)"), x, y);
            return;
        }
        // Over an upgrade slot, explain what that slot upgrades (and what the current contents will do).
        if (hoveredSlot != null) {
            int id = menu.slots.indexOf(hoveredSlot);
            if (id == SoulCageMenu.STEEL_SLOT || id == SoulCageMenu.NETHERITE_SLOT) {
                graphics.setComponentTooltipForNextFrame(font, upgradeTooltip(id, hoveredSlot), x, y);
                return;
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    private List<Component> upgradeTooltip(int id, Slot slot) {
        List<Component> lines = new ArrayList<>();
        ItemStack held = slot.getItem();
        SoulCageBlockEntity cage = menu.getCage();
        Component ingotNote = Component.literal("Block or ingot ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("(" + SoulCageMenu.INGOTS_PER_BLOCK + " ingots = 1 block)")
                        .withStyle(ChatFormatting.DARK_GRAY));
        if (id == SoulCageMenu.STEEL_SLOT) {
            lines.add(Component.literal("Soul Steel").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            lines.add(Component.literal("Expands the area mobs can spawn in").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            lines.add(Component.literal("Each upgrade adds ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("+1 block").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" of spawn range").withStyle(ChatFormatting.DARK_GRAY)));
            lines.add(Component.literal("Each level ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("doubles").withStyle(ChatFormatting.RED))
                    .append(Component.literal(" the steel needed (2x the last)").withStyle(ChatFormatting.DARK_GRAY)));
            if (cage != null && cage.spawnRangeBonus < SoulCageMenu.MAX_RANGE_BONUS) {
                int stepIngots = SoulCageMenu.rangeStepIngots(cage.spawnRangeBonus + 1);
                int remaining = Math.max(0, stepIngots - cage.pendingSteelIngots);
                lines.add(Component.literal("Current bonus: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("+" + cage.spawnRangeBonus + " range").withStyle(ChatFormatting.YELLOW)));
                lines.add(Component.literal("Next level: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("+" + (cage.spawnRangeBonus + 1) + " range").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" costs " + (stepIngots / SoulCageMenu.INGOTS_PER_BLOCK) + " blocks")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(" (" + stepIngots + " ingots)").withStyle(ChatFormatting.DARK_GRAY)));
                lines.add(Component.literal("Banked: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(cage.pendingSteelIngots + " / " + stepIngots + " ingots")
                                .withStyle(ChatFormatting.GREEN)));
                lines.add(Component.literal(remaining + " ingot" + (remaining == 1 ? "" : "s") + " left until next upgrade")
                        .withStyle(ChatFormatting.AQUA));
            } else if (cage != null) {
                lines.add(Component.literal("Range maxed (+" + SoulCageMenu.MAX_RANGE_BONUS + ")")
                        .withStyle(ChatFormatting.GOLD));
            }
            lines.add(ingotNote);
            if (!held.isEmpty()) {
                int perItem = held.is(SpiritBlocks.SOUL_STEEL_BLOCK.get().asItem()) ? SoulCageMenu.INGOTS_PER_BLOCK : 1;
                lines.add(Component.literal("On Confirm: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("banks +" + (held.getCount() * perItem)).withStyle(ChatFormatting.GREEN)));
            }
        } else {
            lines.add(Component.literal("Netherite").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            lines.add(Component.literal("Shortens spawn delay").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            lines.add(Component.literal("Block: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("-1.8s max").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(", -0.2s min").withStyle(ChatFormatting.DARK_AQUA)));
            lines.add(Component.literal("Ingot: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("-0.2s max").withStyle(ChatFormatting.AQUA)));
            lines.add(Component.literal("Floor " + String.format("%.1f", SoulCageMenu.MIN_DELAY_FLOOR / 20.0) + "s")
                    .withStyle(ChatFormatting.DARK_GRAY));
            lines.add(ingotNote);
        }
        return lines;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Reset icon button (far left) — wipes upgrades to tier defaults (no refund).
        boolean resetHover = isOver(mouseX, mouseY, RESET_X, RESET_Y, RESET_W, RESET_H);
        graphics.fill(RESET_X, RESET_Y, RESET_X + RESET_W, RESET_Y + RESET_H, resetHover ? 0xFFAA3030 : 0xCC4A1414);
        drawBorder(graphics, RESET_X, RESET_Y, RESET_W, RESET_H, 0xFF7A2424);
        drawResetIcon(graphics, RESET_X, RESET_Y, RESET_W, RESET_H);

        // Confirm button (middle) — applies (+) or removes (-) depending on the mode toggle.
        boolean confirmHover = isOver(mouseX, mouseY, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H);
        int confirmBg = addMode
                ? (confirmHover ? 0xFF2E9B57 : 0xCC1E6B3C)
                : (confirmHover ? 0xFFC8782E : 0xCC8A521E);
        graphics.fill(CONFIRM_X, CONFIRM_Y, CONFIRM_X + CONFIRM_W, CONFIRM_Y + CONFIRM_H, confirmBg);
        drawBorder(graphics, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H, addMode ? 0xFF35B36A : 0xFFD79A4E);
        String confirmText = addMode ? "Confirm" : "Lower";
        int tw = font.width(confirmText);
        graphics.drawString(font, confirmText, CONFIRM_X + (CONFIRM_W - tw) / 2, CONFIRM_Y + 3, 0xFFEAFFF1, false);

        // +/- mode toggle (far right).
        boolean toggleHover = isOver(mouseX, mouseY, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H);
        int toggleBg = addMode
                ? (toggleHover ? 0xFF2E9B57 : 0xCC1E6B3C)
                : (toggleHover ? 0xFFAA3030 : 0xCC7A2020);
        graphics.fill(TOGGLE_X, TOGGLE_Y, TOGGLE_X + TOGGLE_W, TOGGLE_Y + TOGGLE_H, toggleBg);
        drawBorder(graphics, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H, addMode ? 0xFF35B36A : 0xFFB35050);
        drawPlusMinus(graphics, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H, addMode);

        SoulCageBlockEntity cage = menu.getCage();
        Level level = cage != null ? cage.getLevel() : null;
        if (cage == null || level == null) {
            return;
        }

        ItemStack crystal = cage.getItem(0);
        Tier tier = SoulUtils.getTier(crystal, level);
        Tier next = SoulUtils.getNextTier(crystal, level);
        int souls = SoulUtils.getSoulsInCrystal(crystal);

        // Title + live stats packed into the header strip (inner area ~y10..50), four tight lines.
        Component name = cage.type != null ? cage.type.getDescription() : crystal.getHoverName();
        graphics.drawString(font, name, 14, 10, 0xFFFFFFFF, true);

        String tierName = tier == null ? "-" : Component.translatable(tier.displayName()).getString();
        String soulsLine = next != null ? souls + " / " + next.requiredSouls() + " souls" : souls + " souls";
        graphics.drawString(font, "Tier: " + tierName + "   " + soulsLine, 14, 21, 0xFF55FFFF, true);

        int baseRange = tier != null ? tier.spawnRange() : 0;
        int effRange = baseRange + cage.spawnRangeBonus;
        String rangeStr = "Range " + effRange + (cage.spawnRangeBonus > 0 ? "(+" + cage.spawnRangeBonus + ")" : "");
        String timeStr = "";
        if (tier != null) {
            int minReduction = Math.max(0, cage.minDelayReductionTicks);
            int min = Math.max(SoulCageMenu.MIN_DELAY_FLOOR, tier.minSpawnDelay() - minReduction);
            int max = Math.max(min, tier.maxSpawnDelay() - cage.maxDelayReductionTicks
                    + Math.max(0, cage.rangeDelayPenaltyTicks));
            timeStr = String.format("  Time %.1f-%.1fs", min / 20.0f, max / 20.0f);
        }
        graphics.drawString(font, rangeStr + timeStr, 14, 32, 0xFFFFFFFF, true);

        // Banked soul steel toward the next (exponential) range level — shows where confirmed steel goes.
        String bankLine;
        if (cage.spawnRangeBonus >= SoulCageMenu.MAX_RANGE_BONUS) {
            bankLine = "Range maxed (+" + SoulCageMenu.MAX_RANGE_BONUS + ")";
        } else {
            int nextStep = SoulCageMenu.rangeStepIngots(cage.spawnRangeBonus + 1);
            bankLine = "Next +1 range: " + cage.pendingSteelIngots + "/" + nextStep + " soul steel";
        }
        graphics.drawString(font, bankLine, 14, 43, 0xFFE0B040, true);
    }

    private boolean isOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        int lx = mouseX - leftPos;
        int ly = mouseY - topPos;
        return lx >= x && lx < x + w && ly >= y && ly < y + h;
    }

    /** 1px rectangular border around a button. */
    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** A small white circular "reset" arrow centered in the button. */
    private void drawResetIcon(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        double r = 3.6;
        for (int a = 0; a < 360; a += 7) {
            if (a >= 50 && a <= 100) {
                continue; // gap at the top for the arrowhead
            }
            double rad = Math.toRadians(a);
            int px = (int) Math.round(cx + r * Math.cos(rad));
            int py = (int) Math.round(cy - r * Math.sin(rad));
            g.fill(px, py, px + 1, py + 1, 0xFFEDEDED);
        }
        // Arrowhead at the top of the ring, suggesting clockwise rotation.
        int hx = cx + 2;
        int hy = cy - (int) Math.round(r);
        g.fill(hx - 1, hy, hx + 2, hy + 1, 0xFFEDEDED);
        g.fill(hx, hy - 1, hx + 1, hy + 2, 0xFFEDEDED);
    }

    /** Draws a white "+" (add mode) or "-" (remove mode) centered in the toggle button. */
    private void drawPlusMinus(GuiGraphics g, int x, int y, int w, int h, boolean plus) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        g.fill(cx - 4, cy - 1, cx + 4, cy + 1, 0xFFFFFFFF); // horizontal bar
        if (plus) {
            g.fill(cx - 1, cy - 4, cx + 1, cy + 4, 0xFFFFFFFF); // vertical bar
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && minecraft != null && minecraft.gameMode != null) {
            int mx = (int) event.x();
            int my = (int) event.y();
            if (isOver(mx, my, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H)) {
                addMode = !addMode; // client-side mode flip; Confirm sends the matching button id
                return true;
            }
            if (isOver(mx, my, RESET_X, RESET_Y, RESET_W, RESET_H)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SoulCageMenu.BUTTON_RESET);
                return true;
            }
            if (isOver(mx, my, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        addMode ? SoulCageMenu.BUTTON_CONFIRM : SoulCageMenu.BUTTON_DECREASE);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
