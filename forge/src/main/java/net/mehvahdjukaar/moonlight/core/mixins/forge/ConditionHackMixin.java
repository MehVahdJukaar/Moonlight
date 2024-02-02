package net.mehvahdjukaar.moonlight.core.mixins.forge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

//TODO: figure out
@Mixin(SimplePreparableReloadListener.class)
public abstract class ConditionHackMixin {

    //TODO: refactor in 1.20 and mixin into forge side instead. Then use fabric stuff everywhere directly
    //TODO: !! use forge systme. it has a nice codec conditions sytem that can be used in codecs directly. Is this even needed?
    //literally copies what fabric does

    @Inject(at = @At("HEAD"), method = {"lambda$reload$1", "m_10789_", "method_18790"}) //lambda$reload$1
    private void applyResourceConditions(ResourceManager resourceManager, ProfilerFiller profiler, Object object, CallbackInfo ci) {
        if ((Object) this instanceof SimpleJsonResourceReloadListener) {

            Iterator<Map.Entry<ResourceLocation, JsonElement>> it = ((Map<ResourceLocation, JsonElement>) object).entrySet().iterator();

            var contextRes = ConditionalOps.retrieveContext().codec()
                    .decode(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).result();
            if (contextRes.isEmpty()) return;
            ICondition.IContext context = contextRes.get().getFirst();



            while (it.hasNext()) {
                Map.Entry<ResourceLocation, JsonElement> entry = it.next();
                JsonElement resourceData = entry.getValue();
                if (resourceData.isJsonObject()) {
                    JsonObject obj = resourceData.getAsJsonObject();
                    var conditionHolder = obj.get("global_conditions");
                    if (conditionHolder != null) {
                        var cond = ICondition.LIST_CODEC.decode(JsonOps.INSTANCE, conditionHolder).result();
                        if (cond.isPresent()) {
                            final List<ICondition> conditions = cond.get().getFirst();
                            final boolean conditionsMatch = conditions.stream().allMatch(c -> c.test(context));
                            if (!conditionsMatch) it.remove();
                        }
                    }
                }
            }
        }
    }
}
