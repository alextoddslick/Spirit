package me.codexadrian.spirit.utils;

import me.codexadrian.spirit.data.MobTrait;
import me.codexadrian.spirit.data.MobTraitData;
import me.codexadrian.spirit.data.ToolType;
import me.codexadrian.spirit.registry.SpiritItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ToolUtils {

    @NotNull
    public static InteractionResult handleToolDrawing(Player player,
            @NotNull InteractionHand interactionHand) {
        ItemStack crystal = SoulUtils.findCrystal(player, null, true);
        if (player.getAbilities().instabuild || (!crystal.isEmpty() && SoulUtils.getSoulsInCrystal(crystal) > 0)) {
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    public static InteractionResult handleOnHitBlock(InteractionResult result, ToolType type, Player player,
            ItemStack tool, Level level, BlockPos pos) {
        if (result.consumesAction()) {
            ItemStack soulCrystal = SoulUtils.findCrystal(player, null, true, true, false);
            if (soulCrystal.is(SpiritItems.SOUL_CRYSTAL.get()) && SoulUtils.getSoulsInCrystal(soulCrystal) > 0) {
                if (SoulUtils.getTag(tool).getBooleanOr("Charged", false)) {
                    var entityEffect = BuiltInRegistries.ENTITY_TYPE.get(Identifier
                                    .tryParse(Objects.requireNonNull(SoulUtils.getSoulCrystalType(soulCrystal))))
                                    .map(net.minecraft.core.Holder::value)
                                    .flatMap(et -> MobTraitData.getEffectForEntity(et, ((ServerLevel) level).recipeAccess()));
                    if (entityEffect.isPresent()) {
                        for (MobTrait<?> trait : entityEffect.get().traits()) {
                            trait.onHitBlock(type, player, level.getBlockState(pos), level, pos);
                        }
                        SoulUtils.deviateSoulCount(soulCrystal, -1, level, null);
                        spawnParticles(player);
                    }
                }
            }
        }
        return result;
    }

    public static void handleBreakBlock(Player player, ToolType type, ItemStack itemStack, BlockState blockState,
            Level level, BlockPos blockPos) {
        ItemStack soulCrystal = SoulUtils.findCrystal(player, null, true, true, false);
        if (soulCrystal.is(SpiritItems.SOUL_CRYSTAL.get()) && SoulUtils.getSoulsInCrystal(soulCrystal) > 0) {
            if (SoulUtils.getTag(itemStack).getBooleanOr("Charged", false)) {
                var entityEffect = BuiltInRegistries.ENTITY_TYPE.get(Identifier
                                .tryParse(Objects.requireNonNull(SoulUtils.getSoulCrystalType(soulCrystal))))
                                .map(net.minecraft.core.Holder::value)
                                .flatMap(et -> MobTraitData.getEffectForEntity(et, ((ServerLevel) level).recipeAccess()));
                if (entityEffect.isPresent()) {
                    for (MobTrait<?> trait : entityEffect.get().traits()) {
                        trait.onHitBlock(type, player, blockState, level, blockPos);
                    }
                    SoulUtils.deviateSoulCount(soulCrystal, -1, level, null);
                    spawnParticles(player);
                }
            }
        }
    }

    public static void handleOnHitEntity(ItemStack itemStack, ToolType type, LivingEntity victim, Player player) {
        ItemStack soulCrystal = SoulUtils.findCrystal(player, null, true, true, false);
        if (!soulCrystal.isEmpty() && soulCrystal.is(SpiritItems.SOUL_CRYSTAL.get())
                && SoulUtils.getSoulsInCrystal(soulCrystal) > 0) {
            if (SoulUtils.getTag(itemStack).getBooleanOr("Charged", false)) {
                var entityEffect = BuiltInRegistries.ENTITY_TYPE.get(Identifier
                                .tryParse(Objects.requireNonNull(SoulUtils.getSoulCrystalType(soulCrystal))))
                                .map(net.minecraft.core.Holder::value)
                                .flatMap(et -> MobTraitData.getEffectForEntity(et, ((ServerLevel) player.level()).recipeAccess()));
                if (entityEffect.isPresent()) {
                    for (MobTrait<?> trait : entityEffect.get().traits()) {
                        trait.onHitEntity(type, player, victim);
                    }
                    SoulUtils.deviateSoulCount(soulCrystal, -1, player.level(), null);
                    spawnParticles(player);
                }
            }
        }
    }

    public static void appendEmpoweredText(@NotNull ItemStack itemStack, @NotNull Consumer<Component> list) {
        if (SoulUtils.getTag(itemStack).getBooleanOr("Charged", false)) {
            list.accept(Component.translatable("spirit.item.soul_steel_tool.empowered",
                    Component.keybind("key.spirit.toggle").withStyle(ChatFormatting.RED)));
        } else {
            list.accept(Component.translatable("spirit.item.soul_steel_tool.unpowered",
                    Component.keybind("key.spirit.toggle").withStyle(ChatFormatting.AQUA)));
        }
        Component description = Component.translatable("item.spirit.soul_steel_tools.description")
                .withStyle(ChatFormatting.GRAY);
        Component soulSteelRepairable = Component.translatable("item.spirit.soul_steel_tools.soul_fire_repairable")
                .withStyle(ChatFormatting.GRAY);
        ClientUtils.shiftTooltip(list, List.of(description, soulSteelRepairable), List.of());
    }

    public static void spawnParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (double i = 0; i < 1; i += 0.1) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        player.getX() + Math.sin(i * 2 * Math.PI),
                        player.getY() + 0.75,
                        player.getZ() + Math.cos(i * 2 * Math.PI),
                        1,
                        0,
                        0,
                        0,
                        0);
            }
        }
    }
}
