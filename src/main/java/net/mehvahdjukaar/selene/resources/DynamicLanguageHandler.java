package net.mehvahdjukaar.selene.resources;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.client.language.AfterLanguageLoadEvent;
import net.mehvahdjukaar.selene.resources.pack.DynClientResourcesProvider;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DynamicLanguageHandler {

    private static final ConcurrentLinkedDeque<DynClientResourcesProvider> PACKS = new ConcurrentLinkedDeque<>();

    public static void register(DynClientResourcesProvider rpAwareDynamicTextureProvider) {
        PACKS.add(rpAwareDynamicTextureProvider);
    }
    //TODO: figure out why event isnt working well

    //called by mixin
    @ApiStatus.Internal
    public static void addDynamicEntries(ResourceManager cachedResourceManager, List<LanguageInfo> cachedLanguageInfo, Map<String, String> map) {
        AfterLanguageLoadEvent languageEvent = new AfterLanguageLoadEvent(map, cachedLanguageInfo);
        BlockSetManager.getRegistries().forEach(r -> r.addTypeTranslations(languageEvent));
        //MinecraftForge.EVENT_BUS.post(languageEvent);
        // Selene.LOGGER.info("Dispatching AfterLanguageLoad Event");
        PACKS.forEach(p -> p.addDynamicTranslations(languageEvent));

    }

}
