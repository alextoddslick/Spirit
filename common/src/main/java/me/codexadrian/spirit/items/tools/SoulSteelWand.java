package me.codexadrian.spirit.items.tools;

import me.codexadrian.spirit.utils.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SoulSteelWand extends Item {
    public SoulSteelWand(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> list,
            @NotNull TooltipFlag tooltipFlag) {
        ClientUtils.shiftTooltip(list,
                List.of(Component.translatable("item.spirit.soul_steel_wand.desc").withStyle(ChatFormatting.GRAY)),
                List.of());
    }
}
