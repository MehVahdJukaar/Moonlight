package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.JavaOps;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguagesMixin {

    @ModifyArg(method = "loadFrom",
            at = @At(value = "INVOKE",
                    ordinal = 0,
                    target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private static Map<String, String> moonlight$addDynamicEntries(Map<String, String> map,
                                                                   @Local(argsOnly = true) List<String> languageInfo,
                                                                   @Share("event") LocalRef<AfterLanguageLoadEvent> eventRef) {
        AfterLanguageLoadEvent event = new AfterLanguageLoadEvent(map, languageInfo);
        if (event.isDefault()) {
            //dispatch event and calls listeners
            //has the highest priority
            BlockSetAPI.getRegistries().forEach(r -> r.addTypeTranslations(event));

            MoonlightEventsHelper.postEvent(event, AfterLanguageLoadEvent.class);
        }
        eventRef.set(event);
        return map;
    }

    // cant do this with same mixin because o some arcane issue with Locals not working in production
    @ModifyArg(method = "loadFrom",
            at = @At(value = "INVOKE",
                    ordinal = 1,
                    target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private static Map<String, Component> moonlight$addDynamicEntries2(Map<String, Component> map,
                                                                    @Share("event") LocalRef<AfterLanguageLoadEvent> eventRef) {
        eventRef.get().getExtraLanguageLines().forEach((k, v) -> {
            Component component = ComponentSerialization.CODEC
                    .parse(JavaOps.INSTANCE, v)
                    .getOrThrow(msg -> new JsonParseException("Error parsing translation for " + k + ": " + msg));
            map.put(k, component);
        });
        return map;
    }
}
