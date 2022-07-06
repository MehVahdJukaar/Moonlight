package net.mehvahdjukaar.moonlight.platform.configs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.resources.DynamicLanguageHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ConfigBuilder {

    protected final Map<String, String> comments = new HashMap<>();
    private String currentComment;
    private String currentKey;

    @ExpectPlatform
    public static ConfigBuilder create(ResourceLocation name, ConfigBuilder.ConfigType type) {
        throw new AssertionError();
    }

    private final String name;
    private final String modId;
    protected final ConfigType type;

    public ConfigBuilder(ResourceLocation name, ConfigType type) {
        this.name = name.getPath();
        this.modId = name.getNamespace();
        this.type = type;
        DynamicLanguageHandler.addListener(e -> {
            if (e.isDefault()) comments.forEach(e::addEntry);
        });
    }

    public enum ConfigType {
        CLIENT, COMMON;
    }

    protected String getFileName() {
        return this.modId + "-" + name + ".json";
    }

    public abstract Object buildAndRegister();

    public abstract Object build();

    public String getName() {
        return name;
    }

    public String getModId() {
        return modId;
    }

    public abstract ConfigBuilder push(String category);

    public ConfigBuilder pushPage(String category) {
        push(category);
        return this;
    }

    public abstract ConfigBuilder pop();

    public abstract Supplier<Boolean> define(String name, boolean defaultValue);

    public abstract Supplier<Double> define(String name, double defaultValue, double min, double max);

    public abstract Supplier<Integer> define(String name, int defaultValue, int min, int max);

    public abstract Supplier<String> define(String name, String defaultValue, Predicate<Object> validator);

    public Supplier<String> define(String name, String defaultValue) {
        return define(name, defaultValue, s -> true);
    }

    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue) {
        return define(name, defaultValue, s -> true);
    }

    protected abstract String currentCategory();

    public abstract <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate);

    public abstract <V extends Enum<V>> Supplier<V> define(String name, V defaultValue);


    public Component description(String name) {
        return Component.translatable(translationKey(name));
    }

    public Component tooltip(String name) {
        return Component.translatable(tooltipKey(name));
    }

    public String tooltipKey(String name) {
        return "config." + this.modId + "." + currentCategory() + "." + name + ".description";
    }

    public String translationKey(String name) {
        return "config." + this.modId + "." + currentCategory() + "." + name;
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

    ;

    protected void maybeAddComment(String name) {
        var tooltipKey = this.tooltipKey(name);
        this.currentKey = tooltipKey;
        if (this.currentComment != null && this.currentKey != null) {
            comments.put(currentKey, currentComment);
            this.currentComment = null;
            this.currentKey = null;
        }
    }

    public static final Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static final Predicate<Object> LIST_STRING_CHECK = (s) -> {
        if (s instanceof List<?>) {
            return ((Collection<?>) s).stream().allMatch(o -> o instanceof String);
        }
        return false;
    };

    public static final Predicate<Object> COLOR_CHECK = s -> {
        try {
            Integer.parseUnsignedInt(((String) s).replace("0x", ""), 16);
            return true;
        } catch (Exception e) {
            return false;
        }
    };
}
