package earth.terrarium.spirit.common.item.tools;

<<<<<<< Updated upstream:common/src/main/java/earth/terrarium/spirit/common/item/tools/ScytheMaterial.java
import earth.terrarium.spirit.common.registry.SpiritItems;
=======
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
>>>>>>> Stashed changes:common/src/main/java/me/codexadrian/spirit/items/SoulMetalMaterial.java
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class ScytheMaterial implements Tier {
    public static final ScytheMaterial INSTANCE = new ScytheMaterial();

    @Override
    public int getUses() {
        return 768;
    }

    @Override
    public float getSpeed() {
        return 12;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5;
    }

<<<<<<< Updated upstream:common/src/main/java/earth/terrarium/spirit/common/item/tools/ScytheMaterial.java
    @Override
    public int getLevel() {
        return 2;
    }

    @Override
=======
>>>>>>> Stashed changes:common/src/main/java/me/codexadrian/spirit/items/SoulMetalMaterial.java
    public int getEnchantmentValue() {
        return 20;
    }

    public Ingredient getRepairIngredient() {
        return Ingredient.of(SpiritItems.CRYSTAL_SHARD.get());
    }
<<<<<<< Updated upstream:common/src/main/java/earth/terrarium/spirit/common/item/tools/ScytheMaterial.java
}
=======

    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }
}
>>>>>>> Stashed changes:common/src/main/java/me/codexadrian/spirit/items/SoulMetalMaterial.java
