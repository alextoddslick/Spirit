package me.codexadrian.spirit.items.tools;

import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.items.SoulMetalMaterial;
import me.codexadrian.spirit.utils.SoulUtils;
import me.codexadrian.spirit.utils.ToolUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SoulSteelSword extends Item {
    public SoulSteelSword(Properties properties) {
        super(properties.sword(SoulMetalMaterial.INSTANCE, 3, -2.4F));
    }

    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        return Spirit.SOUL_COLOR;
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity victim, LivingEntity attacker) {
        if (attacker instanceof Player player)
            ToolUtils.handleOnHitEntity(itemStack, ToolType.SWORD, victim, player);
        super.hurtEnemy(itemStack, victim, attacker);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, list, tooltipFlag);
        ToolUtils.appendEmpoweredText(itemStack, list);
    }
}
