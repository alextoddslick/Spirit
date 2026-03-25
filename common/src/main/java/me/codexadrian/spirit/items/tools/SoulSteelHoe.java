package me.codexadrian.spirit.items.tools;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.items.SoulMetalMaterial;
import me.codexadrian.spirit.utils.ToolUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulSteelHoe extends HoeItem {
    public SoulSteelHoe(Properties properties) {
        super(SoulMetalMaterial.INSTANCE,
                properties.attributes(HoeItem.createAttributes(SoulMetalMaterial.INSTANCE, -3, 0F)));
    }

    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        return Spirit.SOUL_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        ToolUtils.appendEmpoweredText(itemStack, list);
        super.appendHoverText(itemStack, context, list, tooltipFlag);
    }
}
