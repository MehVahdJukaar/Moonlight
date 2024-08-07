package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimplePreparableReloadListener.class)
public abstract class ConditionsHackMixin {

/*
    //TODO: refactor in 1.20 and mixin into forge side instead. Then use fabric stuff everywhere directly
    //literally copies what fabric does
    @Inject(at = @At("HEAD"), method = "method_18790")
    private void applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object, CallbackInfo ci) {
        if((Object)this instanceof SimpleJsonResourceReloadListener) {
            Iterator<Map.Entry<ResourceLocation, JsonElement>> it = ((Map<ResourceLocation, JsonElement>) object).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ResourceLocation, JsonElement> entry = it.next();
                JsonElement resourceData = entry.getValue();
                if (resourceData.isJsonObject()) {
                    JsonObject obj = resourceData.getAsJsonObject();
                    if (!ResourceConditionsBridge.matchesForgeCondition(obj)) {
                        it.remove();
                    }
                }
            }
        }
    }*/

}
