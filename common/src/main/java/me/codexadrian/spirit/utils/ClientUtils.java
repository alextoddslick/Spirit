package me.codexadrian.spirit.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static boolean isItemInHand(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return player.getMainHandItem().equals(stack) || player.getOffhandItem().equals(stack);
        }
        return false;
    }

    public static void shiftTooltip(List<Component> components, List<Component> onShiftComponents,
            List<Component> notShiftComponents) {
        Component shiftWord = Component.translatable("misc.spirit.shift.key")
                .withStyle(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT) ? ChatFormatting.WHITE : ChatFormatting.AQUA);
        MutableComponent shift = Component.translatable("misc.spirit.shift.shift_info", shiftWord)
                .withStyle(ChatFormatting.GRAY);
        components.add(shift);
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)) {
            components.addAll(onShiftComponents);
        } else {
            components.addAll(notShiftComponents);
        }
    }

    public static void shiftTooltip(List<Component> components, List<Component> onShiftComponents) {
        shiftTooltip(components, onShiftComponents, List.of());
    }

    public static void shiftTooltip(Consumer<Component> consumer, List<Component> onShiftComponents,
            List<Component> notShiftComponents) {
        Component shiftWord = Component.translatable("misc.spirit.shift.key")
                .withStyle(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT) ? ChatFormatting.WHITE : ChatFormatting.AQUA);
        MutableComponent shift = Component.translatable("misc.spirit.shift.shift_info", shiftWord)
                .withStyle(ChatFormatting.GRAY);
        consumer.accept(shift);
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)) {
            onShiftComponents.forEach(consumer);
        } else {
            notShiftComponents.forEach(consumer);
        }
    }

    public static void shiftTooltip(Consumer<Component> consumer, List<Component> onShiftComponents) {
        shiftTooltip(consumer, onShiftComponents, List.of());
    }

    public static <T extends net.minecraft.world.item.crafting.Recipe<?>> T getRecipe(
            net.minecraft.world.item.crafting.RecipeHolder<T> holder) {
        return holder.value();
    }
}
