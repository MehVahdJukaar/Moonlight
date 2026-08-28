package net.mehvahdjukaar.moonlight.api.client.model.platform;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

public final class CustomUnbakedModelWrapper implements CustomUnbakedBlockStateModel {

    private final CustomUnbakedModel inner;
    private final MapCodec<CustomUnbakedModelWrapper> codec;

    private CustomUnbakedModelWrapper(CustomUnbakedModel inner, MapCodec<CustomUnbakedModelWrapper> codec) {
        this.inner = inner;
        this.codec = codec;
    }

    public static <T extends CustomUnbakedModel> MapCodec<CustomUnbakedModelWrapper> wrap(MapCodec<T> inner) {
        // has to return the codec it was decoded by
        MapCodec<CustomUnbakedModelWrapper>[] self = new MapCodec[1];
        self[0] = inner.xmap(m -> new CustomUnbakedModelWrapper(m, self[0]), w -> (T) w.inner);
        return self[0];
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return this.codec;
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        return new CustomBlockModelWrapper(this.inner.bake(baker));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.inner.resolveDependencies(resolver);
    }
}
