package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DynamicLanguageManager {

    private static final ConcurrentLinkedDeque<RPAwareDynamicTextureProvider> PACKS = new ConcurrentLinkedDeque<>();

    public static void register(RPAwareDynamicTextureProvider rpAwareDynamicTextureProvider) {
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


    //TODO: reformat with proper name
    @Deprecated
    public static class LanguageAccessor extends Event {

        private final Map<String, String> languageLines;
        private final List<LanguageInfo> languageInfo;

        public LanguageAccessor(Map<String, String> lines, List<LanguageInfo> info) {
            this.languageInfo = info;
            this.languageLines = lines;
        }

        @Nullable
        public String getEntry(String key) {
            return languageLines.get(key);
        }

        public void addEntry(String key, String translation) {
            if(!languageLines.containsKey(key)) languageLines.put(key, translation);
        }

        public void addEntries(LangBuilder builder) {
            builder.entries().forEach(this::addEntry);
        }

        public boolean isDefault() {
            return languageInfo.stream().anyMatch(l -> l.getCode().equals("en_us"));
        }

    }
}
