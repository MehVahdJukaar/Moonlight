package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

public interface IOptionalEntityBlock {
    boolean shouldHaveBlockEntity(BlockBehaviour.BlockStateBase state);
}
