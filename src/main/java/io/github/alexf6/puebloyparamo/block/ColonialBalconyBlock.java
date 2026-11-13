package io.github.alexf6.puebloyparamo.block;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Balcón de madera pintada al estilo colonial (conecta automáticamente en pasamanos y esquinas).
 */
public class ColonialBalconyBlock extends FenceBlock {

    public ColonialBalconyBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
