package me.codexadrian.spirit.items;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.utils.ClientUtils;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CrudeSoulCrystalItem extends Item {
    public CrudeSoulCrystalItem(Properties $$0) {
        super($$0);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull net.minecraft.world.item.component.TooltipDisplay tooltipDisplay,
            @NotNull Consumer<Component> list, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, list, tooltipFlag);
        int souls = SoulUtils.getSoulsInCrystal(itemStack);
        if (souls != 0) {
            MutableComponent tooltip = Component.translatable("spirit.item.crude_soul_crystal.tooltip");
            tooltip.append(Component.literal(souls + "/" + SpiritConfig.getCrudeSoulCrystalCap()));
            list.accept(tooltip.withStyle(ChatFormatting.GRAY));
        } else {
            list.accept(Component.translatable("spirit.item.crude_soul_crystal.tooltip_empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        return Spirit.SOUL_COLOR;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack itemStack) {
        return (int) (SoulUtils.getSoulsInCrystal(itemStack) / (double) SpiritConfig.getCrudeSoulCrystalCap() * 13);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack itemStack) {
        return ClientUtils.isItemInHand(itemStack);
    }
}
