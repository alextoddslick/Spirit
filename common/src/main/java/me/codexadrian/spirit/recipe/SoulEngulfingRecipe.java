package me.codexadrian.spirit.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.EngulfableItem;
import me.codexadrian.spirit.data.SyncedData;
import me.codexadrian.spirit.registry.SpiritMisc;
import me.codexadrian.spirit.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;
import java.util.Optional;

public record SoulEngulfingRecipe(SoulEngulfingInput input, int duration, boolean breaksBlocks,
        Item output, int outputAmount)
        implements SyncedData, net.minecraft.world.item.crafting.Recipe<SoulEngulfingRecipe.SoulEngulfingInput> {

    public static com.mojang.serialization.MapCodec<SoulEngulfingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                SoulEngulfingInput.CODEC.fieldOf("input").forGetter(SoulEngulfingRecipe::input),
                Codec.INT.fieldOf("duration").orElse(0).forGetter(SoulEngulfingRecipe::duration),
                Codec.BOOL.fieldOf("destroysStructure").orElse(true).forGetter(SoulEngulfingRecipe::breaksBlocks),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("outputItem").forGetter(SoulEngulfingRecipe::output),
                Codec.INT.fieldOf("outputAmount").orElse(1).forGetter(SoulEngulfingRecipe::outputAmount))
                .apply(instance, SoulEngulfingRecipe::new));
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(this.output, this.outputAmount);
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<SoulEngulfingInput>> getSerializer() {
        return SpiritMisc.SOUL_ENGULFING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends net.minecraft.world.item.crafting.Recipe<SoulEngulfingInput>> getType() {
        return SpiritMisc.SOUL_ENGULFING_RECIPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    // Recipe Interface Implementation
    @Override
    public boolean matches(SoulEngulfingInput input, net.minecraft.world.level.Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return this.input.item.test(input.itemStack)
                    && this.input.multiblock.validateMultiblock(input.blockPos, serverLevel, false);
        }
        return false;
    }

    @Override
    public ItemStack assemble(SoulEngulfingInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return this.getResultItem(registries).copy();
    }


    public boolean validateRecipe(BlockPos blockPos, ItemEntity itemE, ServerLevel level) {
        SoulfireMultiblock multiblock = input().multiblock();
        if (itemE instanceof EngulfableItem engulfableItem) {
            if (!engulfableItem.isEngulfed() && this.duration() > 0) {
                // Engulf state is tracked on the item ENTITY (engulfTime/maxEngulfTime), not on the
                // ItemStack — so we never patch the stack's components (which would break stacking).
                engulfableItem.setMaxEngulfTime(this.duration());
            } else if (engulfableItem.isEngulfed() || this.duration() == 0) {

                // FIX START: Prevent pickup while converting.
                // This prevents the player from grabbing the input item in the same tick
                // the output spawns, which was causing the duplication.
                itemE.setPickUpDelay(20);
                // FIX END

                if (!multiblock.validateMultiblock(blockPos, level, false)) {
                    engulfableItem.resetEngulfing();
                    if (!engulfableItem.isRecipeOutput())
                        itemE.setInvulnerable(false);

                    // FIX: Re-enable pickup if the recipe failed/multiblock broke
                    itemE.setPickUpDelay(0);

                    return false;
                }
                if (engulfableItem.isFullyEngulfed()
                        && multiblock.validateMultiblock(blockPos, level, breaksBlocks())) {
                    itemE.setInvulnerable(true);
                    ItemEntity output = new ItemEntity(itemE.level(), itemE.getX(), itemE.getY(), itemE.getZ(),
                            this.getResultItem(itemE.level().registryAccess()));
                    output.setInvulnerable(true);
                    itemE.level().addFreshEntity(output);
                    if (output instanceof EngulfableItem outputEngulf)
                        outputEngulf.setRecipeOutput();

                    // FIX: Use proper stack shrinking and copy to ensure sync
                    ItemStack stack = itemE.getItem();
                    stack.shrink(1);

                    if (stack.isEmpty()) {
                        itemE.setItem(ItemStack.EMPTY);
                        itemE.discard();
                    } else {
                        itemE.setItem(stack.copy());
                        // Reset invulnerability to allow normal entity behavior (e.g. burning)
                        // This prevents "ghost" items surviving in fire indefinitely with visual
                        // glitches.
                        if (!engulfableItem.isRecipeOutput()) {
                            itemE.setInvulnerable(false);
                        }
                    }

                    engulfableItem.resetEngulfing();
                    level.sendParticles(ParticleTypes.SOUL, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 40, 1, 2,
                            1, 0);
                }
            }
            return true;
        }
        return false;
    }

    public static List<SoulEngulfingRecipe> getRecipesForStack(ItemStack stack, RecipeManager manager) {
        // In 1.21, getAllRecipesFor signature changed - using getRecipes alternative
        return manager.getRecipes().stream()
                .filter(holder -> holder.value() instanceof SoulEngulfingRecipe)
                .map(holder -> (SoulEngulfingRecipe) holder.value())
                .filter(recipe -> recipe.input.item().test(stack)).toList();
    }

    public record SoulEngulfingInput(Ingredient item, SoulfireMultiblock multiblock, ItemStack itemStack,
            BlockPos blockPos) implements net.minecraft.world.item.crafting.RecipeInput {

        // Constructor for Codec (without runtime context)
        public SoulEngulfingInput(Ingredient item, SoulfireMultiblock multiblock) {
            this(item, multiblock, ItemStack.EMPTY, BlockPos.ZERO);
        }

        public static final Codec<SoulEngulfingInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecUtils.INGREDIENT_CODEC.fieldOf("ingredient").forGetter(SoulEngulfingInput::item),
                SoulfireMultiblock.CODEC.fieldOf("multiblock").orElse(SoulfireMultiblock.DEFAULT_RECIPE)
                        .forGetter(SoulEngulfingInput::multiblock))
                .apply(instance, SoulEngulfingInput::new));

        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? itemStack : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return itemStack.isEmpty();
        }
    }
}