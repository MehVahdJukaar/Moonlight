package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.sugar.Local;
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
                                                                   @Local(ordinal = 1) Map<String, Component> componentMap) {
        AfterLanguageLoadEvent event = new AfterLanguageLoadEvent(map, languageInfo);
        if (event.isDefault()) {
            //dispatch event and calls listeners
            //has the highest priority
            BlockSetAPI.getRegistries().forEach(r -> r.addTypeTranslations(event));

            MoonlightEventsHelper.postEvent(event, AfterLanguageLoadEvent.class);
            event.getExtraLanguageLines().forEach((k, v) -> {
                Component component = ComponentSerialization.CODEC
                        .parse(JavaOps.INSTANCE, v)
                        .getOrThrow(msg -> new JsonParseException("Error parsing translation for " + k + ": " + msg));
                componentMap.put(k, component);
            });

        }
        return map;
    }


}
