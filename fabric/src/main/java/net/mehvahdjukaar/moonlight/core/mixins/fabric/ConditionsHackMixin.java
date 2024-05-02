package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.fabric.ResourceConditionsBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

@Mixin(SimplePreparableReloadListener.class)
public abstract class ConditionsHackMixin {

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
    }
}
