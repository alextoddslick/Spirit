package me.codexadrian.spirit.blocks;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * A translucent glass pane made of soul glass. Extends {@link IronBarsBlock} (the vanilla pane base)
 * so it gets the standard connecting blockstate behaviour. A subclass is needed because
 * IronBarsBlock's constructor is protected and cannot be called directly from this package.
 */
public class SoulGlassPaneBlock extends IronBarsBlock {
    public SoulGlassPaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
