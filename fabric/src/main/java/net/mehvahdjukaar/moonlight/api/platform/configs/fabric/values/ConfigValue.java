package net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class ConfigValue<T> extends ConfigEntry implements Supplier<T> {

    protected final T defaultValue;
    protected T value;
    private String translationKey = "";
    private String commentKey = "";
    private String rawComment = "";

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
}
