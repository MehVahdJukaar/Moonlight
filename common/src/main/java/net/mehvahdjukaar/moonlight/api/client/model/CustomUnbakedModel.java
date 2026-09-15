package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

import java.util.function.Supplier;

public interface CustomUnbakedModel extends ResolvableModel {

    CustomBlockModel bake(ModelBaker baker);

    /** The codec this type was registered with. */
    MapCodec<? extends CustomUnbakedModel> codec();

    /** For a model that reads nothing from the json. See NestedUnbakedModel for the one nested model case. */
    static MapCodec<? extends CustomUnbakedModel> constant(Supplier<CustomBlockModel> factory) {
        MapCodec<CustomUnbakedModel>[] self = new MapCodec[1];
        self[0] = MapCodec.unit(() -> new CustomUnbakedModel() {
            @Override
            public CustomBlockModel bake(ModelBaker baker) {
                return factory.get();
            }

            @Override
            public MapCodec<? extends CustomUnbakedModel> codec() {
                return self[0];
            }

            @Override
            public void resolveDependencies(Resolver resolver) {
            }
        });
        return self[0];
    }
}
