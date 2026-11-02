package net.mehvahdjukaar.moonlight.core.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin {

    //neither loader checks conditions on tag entries.
    //hoping this doesnt slow down much
    @ModifyExpressionValue(method = "load", at = @At(value = "INVOKE",
            target = "Lcom/google/gson/JsonParser;parseReader(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"))
    private JsonElement moonlight$applyTagConditions(JsonElement json, @Local(ordinal = 1) ResourceLocation tagId) {
        if (json instanceof JsonObject obj) {
            moonlight$removeFailingEntries(obj.get("values"), tagId);
            moonlight$removeFailingEntries(obj.get("remove"), tagId);
        }
        return json;
    }

    private static void moonlight$removeFailingEntries(JsonElement entries, ResourceLocation tagId) {
        if (!(entries instanceof JsonArray array)) return;
        var it = array.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof JsonObject entry && !PlatHelper.matchesResourceConditions(entry, tagId)) {
                it.remove();
            }
        }
    }
}
