package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.neoforge.ExtraModelDataImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(CustomBakedModel.class)
public interface SelfCustomBakedModel extends IDynamicBakedModel, CustomBakedModel {

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                     ModelData modelData, @Nullable RenderType type) {
        return this.getBlockQuads(state, side, rand, type, new ExtraModelDataImpl(modelData));
    }

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getBlockQuads(state, side, rand, null, ExtraModelData.EMPTY);
    }

    @Override
    default TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return getBlockParticle(new ExtraModelDataImpl(data));
    }

    @Override
    @NotNull
    default ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        ExtraModelData d = modelData == ModelData.EMPTY ? ExtraModelDataImpl.EMPTY : new ExtraModelDataImpl(modelData);
        ExtraModelDataImpl wrapped = (ExtraModelDataImpl) getModelData(d, pos, state, level);
        return wrapped.data();
    }
}
