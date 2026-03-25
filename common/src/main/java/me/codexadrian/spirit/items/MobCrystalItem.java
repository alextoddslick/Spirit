package me.codexadrian.spirit.items;

import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MobCrystalItem extends Item {
    public MobCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context,
            @NotNull net.minecraft.world.item.component.TooltipDisplay tooltipDisplay,
            @NotNull Consumer<Component> list, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, list, tooltipFlag);
        CompoundTag tag = SoulUtils.getTag(itemStack);
        if (tag != null) {
            if (tag.contains("EntityType")) {
                MutableComponent tooltip = Component.translatable("spirit.item.soul_crystal_shard.tooltip",
                        Component.translatable(Util.makeDescriptionId("entity",
                                Identifier.tryParse(tag.getStringOr("EntityType", "")))));
                list.accept(tooltip.withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent unboundTooltip = Component
                        .translatable("spirit.item.crude_soul_crystal.tooltip_empty");
                list.accept(unboundTooltip.withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            MutableComponent unboundTooltip = Component.translatable("spirit.item.crude_soul_crystal.tooltip_empty");
            list.accept(unboundTooltip.withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static float mobCrystalType(ItemStack itemStack, @Nullable ClientLevel clientLevel,
            @Nullable LivingEntity livingEntity, int i) {
        CompoundTag tag = SoulUtils.getTag(itemStack);
        if (tag != null && tag.contains("EntityType")) {
            return 1.0F;
        }
        return 0.0F;
    }
}
