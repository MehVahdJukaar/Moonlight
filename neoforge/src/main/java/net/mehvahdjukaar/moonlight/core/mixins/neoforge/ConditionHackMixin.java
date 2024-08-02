package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;

//TODO: figure out
@Mixin(SimplePreparableReloadListener.class)
public abstract class ConditionHackMixin {

    //TODO: refactor in 1.20 and mixin into forge side instead. Then use fabric stuff everywhere directly
    //literally copies what fabric does
    /*
    @Inject(at = @At("HEAD"), method = {"lambda$reload$1","m_10789_", "method_18790"}) //lambda$reload$1
    private void applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object, CallbackInfo ci) {
        if ((Object) this instanceof SimpleJsonResourceReloadListener) {
            var context = MoonlightForge.getConditionContext();
            if (context == null) return;
            Iterator<Map.Entry<ResourceLocation, JsonElement>> it = ((Map<ResourceLocation, JsonElement>) object).entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<ResourceLocation, JsonElement> entry = it.next();
                JsonElement resourceData = entry.getValue();
                if (resourceData.isJsonObject()) {
                    JsonObject obj = resourceData.getAsJsonObject();
                    if (!CraftingHelper.processConditions(obj, "global_conditions", context)) {
                        it.remove();
                    }
                }
            }
        }
    }*/
}
