package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.model.BakedModelManagerHooks;
import net.fabricmc.fabric.mixin.renderer.client.BakedModelMixin;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MLFabricModelLoaderRegistry {

    private static final Map<ResourceLocation, CustomModelLoader> DESERIALIZER_MAP = new HashMap<>();

    public static void registerLoader(ResourceLocation name, CustomModelLoader deserializer) {
        DESERIALIZER_MAP.put(name, deserializer);
    }

    @Nullable
    public static BlockModel getUnbakedModel(ResourceLocation loader,
                                             JsonDeserializationContext context, JsonObject jsonobject,
                                             BlockModel original) {
        var d = DESERIALIZER_MAP.get(loader);
        if (d == null) {
            Moonlight.LOGGER.error("Unknown model loader: {}", loader);
            return null;
        }
        return new UnbakedModelWrapper(original, d.deserialize(jsonobject,context));
    }
}
