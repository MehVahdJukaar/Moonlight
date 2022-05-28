package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DynamicLanguageManager {

    private static final ConcurrentLinkedDeque<RPAwareDynamicTextureProvider> PACKS = new ConcurrentLinkedDeque<>();

    public static void register(RPAwareDynamicTextureProvider rpAwareDynamicTextureProvider) {
        PACKS.add(rpAwareDynamicTextureProvider);
    }

    //called by mixin
    public static void addDynamicEntries(ResourceManager cachedResourceManager, List<LanguageInfo> cachedLanguageInfo, Map<String, String> map) {
        var lang = new LanguageAccessor(map, cachedLanguageInfo);
        BlockSetManager.getRegistries().forEach(r -> r.addTypeTranslations(lang));
        PACKS.forEach(p -> p.addDynamicTranslations(lang));
    }

    public static class LanguageAccessor {

        private final Map<String, String> languageLines;
        private final List<LanguageInfo> languageInfo;

        private LanguageAccessor(Map<String, String> lines, List<LanguageInfo> info) {
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
