package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.client.model.neoforge.GeometryWrapper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomModelLoader.class)
public interface SelfCustomModelLoaderMixin extends CustomModelLoader, IGeometryLoader<GeometryWrapper> {

    @Override
    default GeometryWrapper read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        return new GeometryWrapper(this.deserialize(jsonObject, context));
    }
}
