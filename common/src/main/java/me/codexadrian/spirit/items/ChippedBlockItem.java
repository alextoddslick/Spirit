package me.codexadrian.spirit.items;

import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.platform.fabric.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ChippedBlockItem extends BlockItem {
    public ChippedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay tooltipDisplay,
            Consumer<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, list, tooltipFlag);
        if (!Services.PLATFORM.isModLoaded("chipped") && SpiritConfig.showChippedError()) {
            list.accept(Component.translatable("spirit.tooltip.chipped_loaded").withStyle(ChatFormatting.RED));
        }
    }
}
