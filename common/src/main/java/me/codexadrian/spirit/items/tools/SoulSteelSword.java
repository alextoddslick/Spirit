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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulSteelSword extends SwordItem {
    public SoulSteelSword(Properties properties) {
        super(SoulMetalMaterial.INSTANCE,
                properties.attributes(SwordItem.createAttributes(SoulMetalMaterial.INSTANCE, 3, -2.4F)));
    }

    @Override
    public int getBarColor(@NotNull ItemStack itemStack) {
        return Spirit.SOUL_COLOR;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity victim, LivingEntity attacker) {
        if (attacker instanceof Player player)
            ToolUtils.handleOnHitEntity(itemStack, ToolType.SWORD, victim, player);
        return super.hurtEnemy(itemStack, victim, attacker);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, list, tooltipFlag);
        ToolUtils.appendEmpoweredText(itemStack, list);
    }
}
