package me.codexadrian.spirit.registry;

// import me.codexadrian.spirit.enchantments.SoulReaperEnchantment; // Enchantments are data-driven in 1.21
import me.codexadrian.spirit.entity.SoulArrowEntity;
import me.codexadrian.spirit.data.CodecRecipeSerializer;
import me.codexadrian.spirit.data.MobTraitData;
import me.codexadrian.spirit.entity.CrudeSoulEntity;
import me.codexadrian.spirit.recipe.PedestalRecipe;
import me.codexadrian.spirit.recipe.SoulEngulfingRecipe;
import me.codexadrian.spirit.data.Tier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

import static me.codexadrian.spirit.platform.fabric.Services.REGISTRY;

public class SpiritMisc {
        public static final Supplier<EntityType<SoulArrowEntity>> SOUL_ARROW_ENTITY = REGISTRY.registerEntity(
                        "soul_arrow",
                        SoulArrowEntity::new, MobCategory.MISC, 0.5F, 0.5F);

        public static final Supplier<EntityType<CrudeSoulEntity>> SOUL_ENTITY = REGISTRY.registerEntity("soul",
                        CrudeSoulEntity::new, MobCategory.MISC, 0.5F, 0.5F);

        public static final Supplier<RecipeType<SoulEngulfingRecipe>> SOUL_ENGULFING_RECIPE = REGISTRY
                        .registerRecipeType("soul_engulfing", () -> new RecipeType<SoulEngulfingRecipe>() {
                                @Override
                                public String toString() {
                                        return "soul_engulfing";
                                }
                        });

        public static final Supplier<RecipeSerializer<SoulEngulfingRecipe>> SOUL_ENGULFING_SERIALIZER = REGISTRY
                        .registerRecipeSerializer("soul_engulfing",
                                        () -> new CodecRecipeSerializer<>(SOUL_ENGULFING_RECIPE.get(),
                                                        SoulEngulfingRecipe.codec()));

        public static final Supplier<RecipeType<Tier>> TIER_RECIPE = REGISTRY.registerRecipeType("soul_cage_tier",
                        () -> new RecipeType<Tier>() {
                                @Override
                                public String toString() {
                                        return "soul_cage_tier";
                                }
                        });

        public static final Supplier<RecipeSerializer<Tier>> TIER_SERIALIZER = REGISTRY.registerRecipeSerializer(
                        "soul_cage_tier",
                        () -> new CodecRecipeSerializer<>(TIER_RECIPE.get(), Tier.codec()));

        public static final Supplier<RecipeType<MobTraitData>> MOB_TRAIT = REGISTRY.registerRecipeType("mob_trait",
                        () -> new RecipeType<MobTraitData>() {
                                @Override
                                public String toString() {
                                        return "mob_trait";
                                }
                        });

        public static final Supplier<RecipeSerializer<MobTraitData>> MOB_TRAIT_SERIALIZER = REGISTRY
                        .registerRecipeSerializer("mob_trait",
                                        () -> new CodecRecipeSerializer<>(MOB_TRAIT.get(), MobTraitData.codec()));

        public static final Supplier<RecipeType<PedestalRecipe>> SOUL_TRANSMUTATION_RECIPE = REGISTRY
                        .registerRecipeType("soul_transmutation", () -> new RecipeType<PedestalRecipe>() {
                                @Override
                                public String toString() {
                                        return "soul_transmutation";
                                }
                        });

        public static final Supplier<RecipeSerializer<PedestalRecipe>> SOUL_TRANSMUTATION_SERIALIZER = REGISTRY
                        .registerRecipeSerializer("soul_transmutation",
                                        () -> new CodecRecipeSerializer<>(SOUL_TRANSMUTATION_RECIPE.get(),
                                                        PedestalRecipe.CODEC));

        // Enchantments are data-driven in 1.21
        public static final net.minecraft.resources.ResourceKey<Enchantment> SOUL_REAPER = net.minecraft.resources.ResourceKey
                        .create(net.minecraft.core.registries.Registries.ENCHANTMENT,
                                        net.minecraft.resources.Identifier.parse(
                                                        me.codexadrian.spirit.Spirit.MODID + ":" + "soul_reaper"));

        public static void registerAll() {
        }
}
