package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.platform.ConfigEntry;
import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigWrapper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class ConfigValue<T> extends ConfigEntry implements Supplier<T>, IConfigWrapper {

    protected final T defaultValue;
    protected T value;
    private boolean loaded;
    private boolean affectsDynamicPacks;
    private boolean gameRestart;
    private boolean worldReload;
    private String translationKey = "";
    private String commentKey = "";
    private String rawComment = "";
    private boolean slider = false;
    private boolean percent = false;

    protected ConfigValue(String name, T defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        if (!(this instanceof ObjectConfigValue<T>) && !(this instanceof JsonConfigValue)) {
            Objects.requireNonNull(defaultValue, "default value cant be null");
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public abstract boolean isValid(T value);

    public void set(T newValue) {
        this.value = newValue;
        this.loaded = true;
    }

    public boolean setAndTrack(T newValue) {
        boolean changed = this.loaded && !Objects.equals(this.value, newValue);
        this.value = newValue;
        this.loaded = true;
        return changed;
    }

    protected void markLoaded() {
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public T get() {
        return value;
    }

    public void setCommentKey(String descriptionKey) {
        this.commentKey = descriptionKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getTranslation() {
        return Component.translatable(translationKey);
    }

    @Nullable
    public Component getCommentComponent() {
        if (this.commentKey.isEmpty()) return null;
        return Component.translatable(commentKey);
    }

    public String getRawComment() {
        return rawComment;
    }

    public void setRawComment(String rawDescription) {
        this.rawComment = rawDescription;
    }

    public String getExtraInfo() {
        return "";
    }

    public boolean isSlider() {
        return slider;
    }

    public void setSlider(boolean slider) {
        this.slider = slider;
    }

    public boolean isPercent() {
        return percent;
    }

    public void setPercent(boolean percent) {
        this.percent = percent;
    }

    @Override
    public boolean affectsDynamicPacks() {
        return affectsDynamicPacks;
    }

    @Override
    public void setAffectsDynamicPacks(boolean affectsDynamicPacks) {
        this.affectsDynamicPacks = affectsDynamicPacks;
    }

    public boolean isGameRestart() {
        return gameRestart;
    }

    public void setGameRestart(boolean gameRestart) {
        this.gameRestart = gameRestart;
    }

    public boolean isWorldReload() {
        return worldReload;
    }

    public void setWorldReload(boolean worldReload) {
        this.worldReload = worldReload;
    }
}
