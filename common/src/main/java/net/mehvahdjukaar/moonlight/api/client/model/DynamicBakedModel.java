package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface DynamicBakedModel extends BakedModel {

    /**
     * Main implementation
     */
    List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource,
                                  RenderType renderType, ExtraModelData extraModelData);


    List<BakedQuad> getItemQuads(ItemStack stack, RandomSource randomSource);
}
