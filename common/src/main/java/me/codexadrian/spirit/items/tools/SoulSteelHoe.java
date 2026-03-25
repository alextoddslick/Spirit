package me.codexadrian.spirit.items.tools;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.items.SoulMetalMaterial;
import me.codexadrian.spirit.utils.ToolUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SoulSteelHoe extends HoeItem {
    public SoulSteelHoe(Properties properties) {
        super(SoulMetalMaterial.INSTANCE, -3, 0F, properties);
    }

    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        return Spirit.SOUL_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> list,
            @NotNull TooltipFlag tooltipFlag) {
        ToolUtils.appendEmpoweredText(itemStack, list);
        super.appendHoverText(itemStack, context, tooltipDisplay, list, tooltipFlag);
    }
}
