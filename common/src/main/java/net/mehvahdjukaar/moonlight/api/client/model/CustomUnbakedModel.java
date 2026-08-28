package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

/**
 * Unbaked CustomBlockModel, read from a blockstate file in place of a model reference. Register its codec with
 * ClientHelper.addBlockModelRegistration. NeoForge dispatches on "type", Fabric on "fabric:type", write both.
 * Nested BlockStateModel.Unbaked fields must be forwarded in resolveDependencies.
 */
public interface CustomUnbakedModel extends ResolvableModel {

    CustomBlockModel bake(ModelBaker baker);

    /** The codec this type was registered with. */
    MapCodec<? extends CustomUnbakedModel> codec();
}
