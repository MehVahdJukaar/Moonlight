package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMeta;
import net.mehvahdjukaar.moonlight.api.platform.configs.WritableConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.ConfigEntry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public abstract class ConfigValue<T> extends ConfigEntry implements WritableConfigValue<T> {

    protected final T defaultValue;
    protected T value;
    private boolean loaded;
    private final ConfigMeta meta;
    private String translationKey = "";
    private String commentKey = "";
    private String rawComment = "";
    private boolean slider = false;
    private boolean percent = false;

    protected ConfigValue(String name, T defaultValue, ConfigMeta meta) {
        super(name);
        this.defaultValue = defaultValue;
        this.meta = meta;
        if (!(this instanceof ObjectConfigValue<T>) && !(this instanceof JsonConfigValue)) {
            Objects.requireNonNull(defaultValue, "default value cant be null");
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public abstract boolean isValid(T value);

    /** Reads this value out of its json element. May throw or return null to fall back to the default. */
    protected abstract T parseValue(JsonElement element) throws Exception;

    /** Serializes this value into the json element written under {@link #getName()}. */
    protected abstract JsonElement encodeValue(T value);

    @Override
    public boolean loadFromJson(JsonObject object) {
        if (object.has(this.name)) {
            try {
                T parsed = parseValue(object.get(this.name));
                if (parsed == null || !isValid(parsed)) parsed = getDefaultValue();
                boolean changed = setValue(parsed);
                markLoaded();
                return this.affectsDynamicPacks() && changed;
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
            }
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
        markLoaded();
        return false;
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = getDefaultValue();
        object.add(this.name, encodeValue(this.value));
    }

    @Override
    public boolean setValue(T newValue) {
        boolean changed = this.loaded && !Objects.equals(this.value, newValue);
        this.value = newValue;
        this.loaded = true;
        return changed;
    }

    /** Void convenience setter (used as a {@code Consumer} by the legacy Cloth/YACL screens). */
    public void set(T newValue) {
        setValue(newValue);
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
        return meta.affectsDynamicPacks();
    }

    @Override
    public ConfigReloadType reloadType() {
        return meta.reloadType();
    }
}
