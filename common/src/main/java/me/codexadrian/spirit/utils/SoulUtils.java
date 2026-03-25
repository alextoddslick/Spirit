package me.codexadrian.spirit.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SoulUtils {
    public static final String SOUL_STEEL_TOOL = "SoulSteelTool";
    public static final String SOUL_CRYSTAL = "SoulCrystal";
    public static final String SOUL_CAGE = "SoulCage";

    public static CompoundTag getTag(ItemStack stack) {
        if (stack.isEmpty())
            return new CompoundTag();
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY);
        return customData.copyTag();
    }

    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (stack.isEmpty())
            return;
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
    }

    @Nullable
    public static Tier getTier(ItemStack itemStack, Level level) {
        CompoundTag tag = getTag(itemStack);
        if (!tag.contains("StoredEntity") && !itemStack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
            System.out.println("SoulUtils: Missing StoredEntity and not Crude Crystal");
            return null;
        }
        int souls = getSoulsInCrystal(itemStack);
        String type = getSoulCrystalType(itemStack);
        System.out.println("SoulUtils: Checking Tier. Souls: " + souls + ", Type: " + type);
        Tier tier = Tier.getTier(souls, type, level);
        System.out.println("SoulUtils: Details - Tier found: " + (tier != null));
        if (tier == null) {
            System.out.println("SoulUtils: Tier verification failed. Tiers available: " + Tier.getTiers(level).size());
        }
        return tier;
    }

    public static String getTierDisplay(ItemStack itemStack, Level level) {
        Tier tier = getTier(itemStack, level);
        return tier == null ? SpiritConfig.getInitialTierName() : tier.displayName();
    }

    @Contract(pure = true)
    @Nullable
    public static Tier getNextTier(ItemStack itemStack, Level level) {
        CompoundTag tag = getTag(itemStack);
        if (!tag.contains("StoredEntity") && !itemStack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
            return null;
        }

        return Tier.getTier(getSoulsInCrystal(itemStack), getSoulCrystalType(itemStack), level, true);
    }

    public static int getMaxSouls(ItemStack itemStack, Level level) {
        CompoundTag tag = getTag(itemStack);
        if (!tag.contains("StoredEntity") && !itemStack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
            return Integer.MAX_VALUE;
        }
        Tier maxTier = Tier.getHighestTier(getSoulCrystalType(itemStack), level);
        return maxTier == null ? Integer.MAX_VALUE : maxTier.requiredSouls();
    }

    public static int getSoulsInCrystal(ItemStack itemStack) {
        CompoundTag tag = getTag(itemStack);
        if (!tag.isEmpty()) {
            if (itemStack.is(SpiritItems.SOUL_CRYSTAL.get())) {
                return tag.getCompoundOrEmpty("StoredEntity").getIntOr("Souls", 0);
            } else if (itemStack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
                return tag.getIntOr("Souls", 0);
            } else if (itemStack.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
                return tag.contains("EntityType") ? 1 : 0;
            }
        }
        return 0;
    }

    public static boolean canCrystalAcceptSoul(ItemStack crystal, @Nullable LivingEntity victim) {
        if (victim == null)
            return canCrystalAcceptSoul(crystal, null, null);
        return canCrystalAcceptSoul(crystal, victim.level(), victim.getType());
    }

    public static boolean canCrystalAcceptSoul(ItemStack crystal, @Nullable Level level, @Nullable EntityType<?> type) {
        if (crystal.is(SpiritItems.SOUL_CRYSTAL.get())) {
            if (type == null)
                return false;
            CompoundTag tag = getTag(crystal);
            if (!tag.isEmpty()) {
                boolean isCorrectType = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()
                        .equals(getSoulCrystalType(crystal));
                boolean hasRoomForMore = getSoulsInCrystal(crystal) < getMaxSouls(crystal, level);
                return isCorrectType && hasRoomForMore;
            }
            if (type.equals(SpiritMisc.SOUL_ENTITY.get()))
                return false;
            // todo check to make sure it isnt of type plain soul
            return true;
        } else if (crystal.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
            return SoulUtils.getSoulsInCrystal(crystal) < SpiritConfig.getCrudeSoulCrystalCap();
        } else if (crystal.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
            return SoulUtils.getSoulCrystalType(crystal) == null;
        }
        return false;
    }

    public static boolean doCrystalTypesMatch(ItemStack crystal1, ItemStack crystal2) {
        return Objects.equals(getSoulCrystalType(crystal1), getSoulCrystalType(crystal2));
    }

    @Nullable
    public static String getSoulCrystalType(ItemStack crystal) {
        CompoundTag tag = getTag(crystal);
        if (crystal.is(SpiritItems.SOUL_CRYSTAL.get())) {
            if (!tag.isEmpty()) {
                String string = tag.getCompoundOrEmpty("StoredEntity").getStringOr("Type", "");
                return string.isBlank() ? null : string;
            }
            return null;
        }
        if (crystal.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
            if (!tag.isEmpty()) {
                String string = tag.getStringOr("EntityType", "");
                return string.isBlank() ? null : string;
            }
            return null;
        }
        return null;
    }

    public static ItemStack findCrystal(Player player, @Nullable LivingEntity victim, boolean mustContainSouls,
            boolean mustBeSoulCrystal, boolean canFindMobCrystal) {
        ItemStack returnStack;
        returnStack = searchTrinkets(player, victim);
        if (returnStack.isEmpty() && canFindMobCrystal)
            returnStack = getMobCrystal(List.of(player.getMainHandItem(), player.getOffhandItem()));
        if (returnStack.isEmpty())
            returnStack = getSoulCrystal(List.of(player.getMainHandItem(), player.getOffhandItem()), victim, mustContainSouls);
        if (returnStack.isEmpty() && !mustBeSoulCrystal)
            returnStack = getCrudeSoulCrystal(List.of(player.getMainHandItem(), player.getOffhandItem()), mustContainSouls);
        if (returnStack.isEmpty() && canFindMobCrystal)
            returnStack = getMobCrystal(player.getInventory().getNonEquipmentItems());
        if (returnStack.isEmpty())
            returnStack = getSoulCrystal(player.getInventory().getNonEquipmentItems(), victim, mustContainSouls);
        if (returnStack.isEmpty() && !mustBeSoulCrystal)
            returnStack = getCrudeSoulCrystal(player.getInventory().getNonEquipmentItems(), mustContainSouls);
        return returnStack;
    }

    public static ItemStack findCrystal(Player player, @Nullable LivingEntity victim, boolean mustContainSouls) {
        return findCrystal(player, victim, mustContainSouls, false, false);
    }

    @ExpectPlatform
    public static ItemStack searchTrinkets(Player player, @Nullable LivingEntity victim) {
        throw new AssertionError();
    }

    public static ItemStack getSoulCrystal(Iterable<ItemStack> inventory, @Nullable LivingEntity victim,
            boolean mustContainSouls) {
        ItemStack savedStack = ItemStack.EMPTY;
        int savedSouls = 0;
        for (ItemStack currentItem : inventory) {
            if (!currentItem.is(SpiritItems.SOUL_CRYSTAL.get())
                    || (getSoulsInCrystal(currentItem) == 0 && mustContainSouls)) {
                continue;
            }
            if (victim == null) {
                return currentItem;
            }
            if (canCrystalAcceptSoul(currentItem, victim)) {
                if (savedStack.isEmpty()) {
                    savedStack = currentItem;
                } else {
                    CompoundTag tag = getTag(currentItem);
                    if (!tag.isEmpty()) {
                        // if the current savedStack either is empty or has some amount of souls in it.
                        // therefore any new crystal that's empty
                        // is either equal to or worse than the saved stack, so the current item, if it
                        // is empty, should be skipped.
                        int souls = getSoulsInCrystal(currentItem);
                        if (souls > savedSouls) {
                            savedStack = currentItem;
                            savedSouls = souls;
                        }
                    }
                }
            }
        }
        return savedStack;
    }

    public static ItemStack getCrudeSoulCrystal(Iterable<ItemStack> inventory, boolean mustContainSouls) {
        ItemStack savedStack = ItemStack.EMPTY;
        int savedSouls = 0;

        for (ItemStack currentItem : inventory) {
            if (!currentItem.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())
                    || (getSoulsInCrystal(currentItem) == 0 && mustContainSouls)) {
                continue;
            }

            if (!SoulUtils.canCrystalAcceptSoul(currentItem, null))
                continue;

            int soulsInCrystal = getSoulsInCrystal(currentItem);
            if (savedStack.isEmpty() || soulsInCrystal > savedSouls) {
                savedStack = currentItem;
                savedSouls = soulsInCrystal;
            }
        }

        return savedStack;
    }

    public static ItemStack getMobCrystal(Iterable<ItemStack> inventory) {
        ItemStack savedStack = ItemStack.EMPTY;
        for (ItemStack currentItem : inventory) {
            if (!currentItem.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
                continue;
            }

            if (savedStack.isEmpty() && getSoulCrystalType(currentItem) == null) {
                savedStack = currentItem;
            }
        }

        return savedStack;
    }

    public static void handleMobCrystal(ItemStack mobCrystal, Player player, LivingEntity victim) {
        if (player.level() instanceof ServerLevel serverLevel) {
            deviateSoulCount(mobCrystal, 1, player.level(),
                    BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
            serverLevel.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY(), victim.getZ(), 20,
                    victim.getBbWidth(), victim.getBbHeight(), victim.getBbWidth(), 0);
            serverLevel.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY(), player.getZ(), 40, 1, 2, 1, 0);
        }
    }

    public static void handleSoulCrystal(ItemStack soulCrystal, Player player, LivingEntity victim) {
        // Gravy seal of okayness
        if (player.level() instanceof ServerLevel serverLevel) {
            CompoundTag tag = getTag(soulCrystal);
            CompoundTag storedEntity;
            if (!tag.contains("StoredEntity")) {
                CompoundTag newTag = new CompoundTag();
                newTag.putString("Type", BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
                tag.put("StoredEntity", newTag);
                storedEntity = newTag;
            } else {
                storedEntity = tag.getCompoundOrEmpty("StoredEntity");
            }

            // We must update the stack with the modified tag immediately if we added
            // "StoredEntity"
            // But we also modify "storedEntity" later, so we should update at the end or
            // intermediate?
            // "storedEntity" is reference to the sub-tag in "tag". So "tag" is updated.

            serverLevel.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY(), victim.getZ(), 20,
                    victim.getBbWidth(), victim.getBbHeight(), victim.getBbWidth(), 0);

            // Note: getNextTier looks at the stack again. If we haven't saved "tag" to
            // "soulCrystal", getNextTier might fail?
            // getNextTier calls getSoulsInCrystal which calls getTag().
            // We should save now to be safe.
            setTag(soulCrystal, tag);

            Tier tier = SoulUtils.getNextTier(soulCrystal, serverLevel);
            int currentSouls = storedEntity.getIntOr("Souls", 0);

            int incrementAmount = getSoulHarvestAmount(player);

            System.out.println("SoulUtils: Harvesting soul. Current: " + currentSouls + ", Added: " + incrementAmount);
            if (tier != null) {
                System.out.println(
                        "SoulUtils: Next Tier found: " + tier.displayName() + ", Req: " + tier.requiredSouls());
            } else {
                System.out.println("SoulUtils: No next tier found.");
            }

            if (tier != null && storedEntity.getIntOr("Souls", 0) + incrementAmount >= tier.requiredSouls()) {
                player.displayClientMessage(Component.translatable("item.spirit.soul_crystal.upgrade_message")
                        .withStyle(ChatFormatting.AQUA), true);
                serverLevel.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY(), player.getZ(), 40, 1, 2, 1,
                        0);
            }

            storedEntity.putInt("Souls", storedEntity.getIntOr("Souls", 0) + incrementAmount);
            setTag(soulCrystal, tag);
        }
    }

    public static void deviateSoulCount(ItemStack stack, int deviation, Level level, @Nullable String mobType) {
        CompoundTag tag = getTag(stack);
        if (stack.is(SpiritItems.SOUL_CRYSTAL_SHARD.get())) {
            if (deviation < 0)
                tag.remove("EntityType");
            else if (mobType != null && deviation > 0)
                tag.putString("EntityType", mobType);
            setTag(stack, tag);
            return;
        }
        if (!tag.isEmpty()) {
            if (stack.is(SpiritItems.SOUL_CRYSTAL.get())) {
                CompoundTag storedEntity = tag.getCompoundOrEmpty("StoredEntity");
                storedEntity.putInt("Souls",
                        Mth.clamp(getSoulsInCrystal(stack) + deviation, 0, SoulUtils.getMaxSouls(stack, level)));
                if (storedEntity.getIntOr("Souls", 0) == 0) {
                    tag = new CompoundTag(); // Clear tag
                }
            } else if (stack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get())) {
                tag.putInt("Souls",
                        Mth.clamp(getSoulsInCrystal(stack) + deviation, 0, SpiritConfig.getCrudeSoulCrystalCap()));
                if (tag.getIntOr("Souls", 0) == 0) {
                    tag = new CompoundTag(); // Clear tag
                }
            }
            setTag(stack, tag);
        } else {
            if (stack.is(SpiritItems.SOUL_CRYSTAL.get()) && mobType != null && deviation > 0) {
                CompoundTag storedEntity = new CompoundTag();
                storedEntity.putString("Type", mobType);
                storedEntity.putInt("Souls", deviation);
                tag.put("StoredEntity", storedEntity);
            } else if (stack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get()) && deviation > 0) {
                tag.putInt("Souls", Math.min(deviation, SpiritConfig.getCrudeSoulCrystalCap()));
            } else if (stack.is(SpiritItems.SOUL_CRYSTAL_SHARD.get()) && deviation > 0 && mobType != null) {
                tag.putString("EntityType", mobType);
            }
            setTag(stack, tag);
        }
    }

    public static void handleCrudeSoulCrystal(ItemStack crudeCrystal, Player player, LivingEntity victim) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY(), victim.getZ(), 20,
                    victim.getBbWidth(), victim.getBbHeight(), victim.getBbWidth(), 0);
            CompoundTag tag = getTag(crudeCrystal);
            tag.putInt("Souls",
                    Math.min(getSoulsInCrystal(crudeCrystal) + getSoulHarvestAmount(player),
                            SpiritConfig.getCrudeSoulCrystalCap()));
            setTag(crudeCrystal, tag);
        }
    }

    public static int getSoulHarvestAmount(Player player) {
        int returnAmount = 1;
        if (player.getMainHandItem().is(Spirit.SOUL_STEEL_MAINHAND)
                || player.getOffhandItem().is(Spirit.SOUL_STEEL_OFFHAND))
            returnAmount++;
        if (player.level().registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .isPresent()) {
            var registry = player.level().registryAccess()
                    .lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).get();
            var holder = registry.get(SpiritMisc.SOUL_REAPER);
            if (holder.isPresent()) {
                return returnAmount + EnchantmentHelper.getItemEnchantmentLevel(holder.get(), player.getMainHandItem());
            }
        }
        return returnAmount;
    }

    public static boolean isAllowed(ItemStack crystal, TagKey<EntityType<?>> blacklistTag) {
        String entityTypeName = getSoulCrystalType(crystal);
        if (crystal.is(SpiritItems.SOUL_CRYSTAL.get()) && entityTypeName != null) {
            return EntityType.byString(entityTypeName).map(entityType -> !entityType.is(blacklistTag)).orElse(false);
        }
        return false;
    }

    public static boolean isEmpowered(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag.getBooleanOr("Charged", false);
    }

    public static void setEmpowered(ItemStack stack, boolean empowered) {
        CompoundTag tag = getTag(stack);
        tag.putBoolean("Charged", empowered);
        setTag(stack, tag);
    }

    public static boolean canCrystalBeUsedInCage(ItemStack stack) {
        return stack.is(SpiritItems.SOUL_CRYSTAL.get()) || stack.is(SpiritItems.CRUDE_SOUL_CRYSTAL.get());
    }
}
