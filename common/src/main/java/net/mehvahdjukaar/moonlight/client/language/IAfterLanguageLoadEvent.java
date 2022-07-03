package net.mehvahdjukaar.moonlight.client.language;

import javax.annotation.Nullable;

public interface IAfterLanguageLoadEvent {

    @Nullable
    public String getEntry(String key);

    public void addEntry(String key, String translation);

    public void addEntries(LangBuilder builder);

    public boolean isDefault();
}