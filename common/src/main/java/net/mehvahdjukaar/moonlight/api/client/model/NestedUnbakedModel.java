package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

import java.util.function.Function;

public final class NestedUnbakedModel implements CustomUnbakedModel {

    private final BlockStateModel.Unbaked model;
    private final Function<BlockStateModel, CustomBlockModel> factory;
    private final MapCodec<NestedUnbakedModel> codec;

    private NestedUnbakedModel(BlockStateModel.Unbaked model, Function<BlockStateModel, CustomBlockModel> factory,
                               MapCodec<NestedUnbakedModel> codec) {
        this.model = model;
        this.factory = factory;
        this.codec = codec;
    }

    public static MapCodec<NestedUnbakedModel> codec(String field, Function<BlockStateModel, CustomBlockModel> factory) {
        MapCodec<NestedUnbakedModel>[] self = new MapCodec[1];
        self[0] = BlockStateModel.Unbaked.CODEC.fieldOf(field).xmap(m -> new NestedUnbakedModel(m, factory, self[0]), n -> n.model);
        return self[0];
    }

    @Override
    public CustomBlockModel bake(ModelBaker baker) {
        return this.factory.apply(this.model.bake(baker));
    }

    @Override
    public MapCodec<? extends CustomUnbakedModel> codec() {
        return this.codec;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.model.resolveDependencies(resolver);
    }
}
