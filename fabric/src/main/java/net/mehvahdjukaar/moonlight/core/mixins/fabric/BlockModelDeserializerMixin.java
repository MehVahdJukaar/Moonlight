package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockModelRendererMixin;
import net.mehvahdjukaar.moonlight.api.client.model.fabric.MLFabricModelLoaderRegistry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(BlockModel.Deserializer.class)
public abstract class BlockModelDeserializerMixin {

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
            at = @At("HEAD"), cancellable = true)
    public void deserialize(JsonElement element, Type targetType, JsonDeserializationContext deserializationContext,
                            CallbackInfoReturnable<BlockModel> cir) throws JsonParseException {
        JsonObject jsonobject = element.getAsJsonObject();
        if (jsonobject.has("loader")) {
            ResourceLocation loader = new ResourceLocation(GsonHelper.getAsString(jsonobject, "loader"));
            BlockModel custom = MLFabricModelLoaderRegistry.getUnbakedModel(
                    loader, deserializationContext, jsonobject, cir.getReturnValue());
            if (custom != null) cir.setReturnValue(custom);
        }
    }
}
