package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CustomBakedModel extends BakedModel {

    /**
     * Main implementation
     */
    List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource,
                                  RenderType renderType, ExtraModelData extraModelData);

    TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData);

    // do not implement
    @ApiStatus.Internal
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return List.of();
    }

    @Override
    default TextureAtlasSprite getParticleIcon() {
        return getBlockParticle(ExtraModelData.EMPTY);
    }

    /**
     * you can override this method to get the data of your block.
     * By default, the implementation just grabs it from the tile entity at pos if it implements IExtraModelDataProvider
     */
    default ExtraModelData getModelData(@NotNull ExtraModelData tileData, BlockPos pos, BlockState state, BlockAndTintGetter level) {
        return tileData;
    }

    default ExtraModelData getModelData(@NotNull ExtraModelData originalData, ItemStack stack) {
        return originalData;
    }
}
