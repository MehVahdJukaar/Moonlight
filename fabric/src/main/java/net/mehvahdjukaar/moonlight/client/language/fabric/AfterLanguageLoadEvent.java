package net.mehvahdjukaar.moonlight.client.language.fabric;

import net.mehvahdjukaar.moonlight.client.language.IAfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.client.language.LangBuilder;
import net.minecraft.client.resources.language.LanguageInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AfterLanguageLoadEvent implements IAfterLanguageLoadEvent {

    private final Map<String, String> languageLines;
    private final List<LanguageInfo> languageInfo;

    public AfterLanguageLoadEvent(Map<String, String> lines, List<LanguageInfo> info) {
        this.languageInfo = info;
        this.languageLines = lines;
    }

    @Nullable
    public String getEntry(String key) {
        return languageLines.get(key);
    }

    public void addEntry(String key, String translation) {
        if (!languageLines.containsKey(key)) languageLines.put(key, translation);
    }

    public void addEntries(LangBuilder builder) {
        builder.entries().forEach(this::addEntry);
    }

    public boolean isDefault() {
        return languageInfo.stream().anyMatch(l -> l.getCode().equals("en_us"));
    }
}