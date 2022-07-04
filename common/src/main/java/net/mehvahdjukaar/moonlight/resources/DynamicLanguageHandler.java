package net.mehvahdjukaar.moonlight.resources;

import net.mehvahdjukaar.moonlight.block_set.BlockSetManager;
import net.mehvahdjukaar.moonlight.client.language.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.resources.pack.DynClientResourcesProvider;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class DynamicLanguageHandler {

    private static final ConcurrentLinkedDeque<Consumer<AfterLanguageLoadEvent>> LISTENERS = new ConcurrentLinkedDeque<>();

    public static void addListener(DynClientResourcesProvider rpAwareDynamicTextureProvider) {
        LISTENERS.add(rpAwareDynamicTextureProvider::addDynamicTranslations);
    }

    public static void addListener(Consumer<AfterLanguageLoadEvent> listener) {
        LISTENERS.add(listener);
    }
    //TODO: figure out why event isnt working well

    //called by mixin
    @ApiStatus.Internal
    public static void addDynamicEntries(ResourceManager cachedResourceManager, List<LanguageInfo> cachedLanguageInfo, Map<String, String> map) {
        AfterLanguageLoadEvent languageEvent = new AfterLanguageLoadEvent(map, cachedLanguageInfo);
        if(languageEvent.isDefault()) {
            BlockSetManager.getRegistries().forEach(r -> r.addTypeTranslations(languageEvent));
            //MinecraftForge.EVENT_BUS.post(languageEvent);
            // Selene.LOGGER.info("Dispatching AfterLanguageLoad Event");
            LISTENERS.forEach(p -> p.accept(languageEvent));
        }

    }

}
