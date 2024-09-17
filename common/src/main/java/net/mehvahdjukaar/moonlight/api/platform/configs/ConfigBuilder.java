package net.mehvahdjukaar.moonlight.api.platform.configs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A loader independent config builder
 * Support common config syncing
 */
public abstract class ConfigBuilder {

    protected final Map<String, String> comments = new HashMap<>();
    private String currentComment;
    private String currentKey;
    protected Runnable changeCallback;

    //always on. can be called to disable
    protected boolean usesDataBuddy = true;

    @ExpectPlatform
    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        throw new AssertionError();
    }

    public static ConfigBuilder create(String modId, ConfigType type) {
        return create(ResourceLocation.fromNamespaceAndPath(modId, type.getDefaultName()), type);
    }

    private final ResourceLocation name;
    protected final ConfigType type;

    protected ConfigBuilder(ResourceLocation name, ConfigType type) {
        this.name = name;
        this.type = type;
        Consumer<AfterLanguageLoadEvent> consumer = e -> {
            if (e.isDefault()) comments.forEach(e::addEntry);
        };
        MoonlightEventsHelper.addListener(consumer, AfterLanguageLoadEvent.class);
    }

    public abstract ModConfigHolder build();

    public ResourceLocation getName() {
        return name;
    }

    public abstract ConfigBuilder push(String category);

    public abstract ConfigBuilder pop();

    public <T extends ConfigBuilder> T setWriteJsons() {
        this.usesDataBuddy = false;
        return (T) this;
    }

    public abstract Supplier<Boolean> define(String name, boolean defaultValue);

    public abstract Supplier<Double> define(String name, double defaultValue, double min, double max);

    public abstract Supplier<Float> define(String name, float defaultValue, float min, float max);

    public abstract Supplier<Integer> define(String name, int defaultValue, int min, int max);

    public abstract Supplier<Integer> defineColor(String name, int defaultValue);

    public abstract Supplier<String> define(String name, String defaultValue, Predicate<Object> validator);

    public Supplier<String> define(String name, String defaultValue) {
        return define(name, defaultValue, STRING_CHECK);
    }

    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue) {
        return define(name, defaultValue, s -> true);
    }

    public abstract String currentCategory();

    public abstract <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate);

    public abstract <V extends Enum<V>> Supplier<V> define(String name, V defaultValue);

    //be very careful with these as you might use some objects that aren't registered yet and things will break
    public abstract <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> codec);

    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        return defineObject(name, defaultSupplier, codec.listOf());
    }

    public Supplier<Map<String, String>> defineMap(String name, Map<String, String> def) {
        return defineObject(name, () -> def, Codec.unboundedMap(Codec.STRING, Codec.STRING));
    }

    public Supplier<Map<ResourceLocation, ResourceLocation>> defineIDMap(String name, Map<ResourceLocation, ResourceLocation> def) {
        return defineObject(name, () -> def, Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC));
    }

    public abstract Supplier<JsonElement> defineJson(String name, JsonElement defaultValue);

    public abstract Supplier<JsonElement> defineJson(String name, Supplier<JsonElement> defaultValue);


    public Supplier<ResourceLocation> define(String name, ResourceLocation defaultValue) {
        return new ResourceLocationConfigValue(this, name, defaultValue);
    }

    private static class ResourceLocationConfigValue implements Supplier<ResourceLocation> {

        private final Supplier<String> inner;
        private ResourceLocation cache;
        private String oldString;

        public ResourceLocationConfigValue(ConfigBuilder builder, String path, ResourceLocation defaultValue) {
            this.inner = builder.define(path, defaultValue.toString(), s -> s != null && ResourceLocation.tryParse((String) s) != null);
        }

        @Override
        public ResourceLocation get() {
            String s = inner.get();
            if (!s.equals(oldString)) cache = null;
            oldString = s;
            if (cache == null) cache = ResourceLocation.parse(s);
            return cache;
        }
    }

    public Component description(String name) {
        return Component.translatable(translationKey(name));
    }

    public Component tooltip(String name) {
        return Component.translatable(tooltipKey(name));
    }

    public String tooltipKey(String name) {
        return "config." + this.name.getNamespace() + "." + currentCategory() + "." + name + ".description";
    }

    public String translationKey(String name) {
        return "config." + this.name.getNamespace() + "." + currentCategory() + "." + name;
    }


    /**
     * Try not to use this. Just here to make porting easier
     * Will add entries manually to the english language file
     */
    public ConfigBuilder comment(String comment) {
        this.currentComment = comment;
        if (this.currentComment != null && this.currentKey != null) {
            comments.put(currentKey, currentComment);
            this.currentComment = null;
            this.currentKey = null;
        }
        return this;
    }

    public ConfigBuilder onChange(Runnable callback) {
        this.changeCallback = callback;
        return this;
    }

    public abstract ConfigBuilder worldReload();

    public abstract ConfigBuilder gameRestart();

    protected void maybeAddTranslationString(String name) {
        this.currentKey = this.tooltipKey(name);
        if (this.currentComment != null && this.currentKey != null) {
            this.comments.put(currentKey, currentComment);
            this.currentComment = null;
            this.currentKey = null;
        }
        if (this.currentCategory() == null && PlatHelper.isDev()) throw new AssertionError("Current config category was null. How?");
    }

    public static final Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static final Predicate<Object> LIST_STRING_CHECK = (s) -> {
        if (s instanceof List<?>) {
            return ((Collection<?>) s).stream().allMatch(o -> o instanceof String);
        }
        return false;
    };

}
