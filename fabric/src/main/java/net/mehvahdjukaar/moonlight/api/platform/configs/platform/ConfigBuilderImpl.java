package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.values.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.http.annotation.Experimental;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final JsonConfigCategory mainCategory = new JsonConfigCategory(this.getName().getNamespace());

    private final Deque<JsonConfigCategory> categoryStack = new ArrayDeque<>();

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        categoryStack.push(mainCategory);
    }

    @Override
    @NotNull
    protected FabricConfigHolder buildHolder() {
        assert categoryStack.size() == 1;
        return new FabricConfigHolder(this.getName(), mainCategory, this.type, this.buildChangeCallback(), getUiRoot());
    }

    @Override
    public ConfigBuilderImpl push(String translation) {
        var cat = new JsonConfigCategory(translation);
        Objects.requireNonNull(categoryStack.peek()).addEntry(cat);
        categoryStack.push(cat);
        enterCategory(translation);
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        flushPendingComment(); // a trailing after-comment in this category has no following define to claim it
        assert categoryStack.size() != 1;
        categoryStack.pop();
        exitCategory();
        return this;
    }

    // The flags stay set while a grouped value defines its hidden parts, so every piece of a range or vec3 gets the
    // same reload info
    private ConfigMetadata pendingMeta() {
        return new ConfigMetadata(this.pendingReload, this.pendingDynamicPacks);
    }

    private void doAddConfig(String name, FabricConfigValue<?> config) {
        doAddConfig(name, config, ConfigBuilderImpl::toOption);
    }

    private void doAddConfig(String name, FabricConfigValue<?> config, Function<FabricConfigValue<?>, ConfigOption<?>> optionFactory) {
        config.setTranslationKey(this.translationKey(name));
        addTranslationsAndComments(name);

        Objects.requireNonNull(this.categoryStack.peek()).addEntry(config);

        // the comment, before or after, fills in the row's description
        if (!suppressUi) {
            ConfigOption<?> option = optionFactory.apply(config);
            recordOption(option);
            noteDefined(name, option, raw -> config.setCommentKey(this.tooltipKey(name)));
        }
    }

    // Description is left empty here: comment(...) fills it in later through noteDefined
    private static ConfigOption<?> toOption(FabricConfigValue<?> v) {
        Component title = v.getTranslation();
        boolean slider = v.isSlider();
        // FabricColorConfigValue extends FabricIntConfigValue, so it must be checked first
        if (v instanceof FabricColorConfigValue c) {
            return new ConfigOption.ColorOption(title, null, c, c.getDefaultValue(), c.hasAlpha());
        } else if (v instanceof FabricIntConfigValue i) {
            return slider
                    ? new ConfigOption.IntSliderOption(title, null, i, i.getDefaultValue(), i.getMin(), i.getMax())
                    : new ConfigOption.IntOption(title, null, i, i.getDefaultValue(), i.getMin(), i.getMax());
        } else if (v instanceof FabricDoubleConfigValue d) {
            if (v.isPercent()) return new ConfigOption.PercentOption(title, null, d, d.getDefaultValue());
            return slider
                    ? new ConfigOption.DoubleSliderOption(title, null, d, d.getDefaultValue(), d.getMin(), d.getMax())
                    : new ConfigOption.DoubleOption(title, null, d, d.getDefaultValue(), d.getMin(), d.getMax());
        } else if (v instanceof FabricFloatConfigValue f) {
            return slider
                    ? new ConfigOption.FloatSliderOption(title, null, f, f.getDefaultValue(), f.getMin(), f.getMax())
                    : new ConfigOption.FloatOption(title, null, f, f.getDefaultValue(), f.getMin(), f.getMax());
        } else if (v instanceof FabricBoolConfigValue b) {
            return new ConfigOption.BooleanOption(title, null, b, b.getDefaultValue());
        } else if (v instanceof FabricEnumConfigValue<?> e) {
            return enumOption(e, title);
        } else if (v instanceof FabricRegexConfigValue r) {
            // must be checked before FabricStringConfigValue (FabricRegexConfigValue extends it)
            return new ConfigOption.RegexOption(title, null, r, r.getDefaultValue());
        } else if (v instanceof FabricDropdownConfigValue d) {
            // also extends FabricStringConfigValue, so check first
            return new ConfigOption.DropdownOption(title, null, d, d.getDefaultValue(), d.getOptions(), d.getIcon());
        } else if (v instanceof FabricStringConfigValue s) {
            return new ConfigOption.StringOption(title, null, s, s.getDefaultValue(),
                    o -> o instanceof String str && s.isValid(str));
        } else if (v instanceof FabricListStringConfigValue<?> l) {
            return new ConfigOption.ListOption(title, null, l, l.getDefaultValue(),
                    l.getPredicate()::test, l.getOptions(), l.getIcon());
        } else if (v instanceof FabricJsonConfigValue j) {
            // raw json + beans (defineBean rides on defineJson) -> editable json text box
            return new ConfigOption.JsonOption(title, null, j);
        }
        // codec objects: not yet editable
        @SuppressWarnings("unchecked")
        Supplier<Object> handle = (Supplier<Object>) v;
        return new ConfigOption.UnsupportedOption(title, null, handle);
    }

    private static <E extends Enum<E>> ConfigOption.EnumOption<E> enumOption(FabricEnumConfigValue<E> e, Component title) {
        return new ConfigOption.EnumOption<>(title, null, e, e.getDefaultValue(), e.getEnumClass().getEnumConstants());
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        var config = new FabricBoolConfigValue(name, defaultValue, pendingMeta());
        doAddConfig(name, config);
        return config;
    }


    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        var config = new FabricDoubleConfigValue(name, defaultValue, min, max, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        var config = new FabricFloatConfigValue(name, defaultValue, min, max, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Double> definePercentage(String name, double defaultValue) {
        var config = new FabricDoubleConfigValue(name, defaultValue, 0.0, 1.0, pendingMeta());
        config.setPercent(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        var config = new FabricIntConfigValue(name, defaultValue, min, max, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> defineSlider(String name, int defaultValue, int min, int max) {
        var config = new FabricIntConfigValue(name, defaultValue, min, max, pendingMeta());
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Double> defineSlider(String name, double defaultValue, double min, double max) {
        var config = new FabricDoubleConfigValue(name, defaultValue, min, max, pendingMeta());
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Float> defineSlider(String name, float defaultValue, float min, float max) {
        var config = new FabricFloatConfigValue(name, defaultValue, min, max, pendingMeta());
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue, boolean hasAlpha) {
        var config = new FabricColorConfigValue(name, defaultValue, hasAlpha, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        var config = new FabricStringConfigValue(name, defaultValue, validator, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<String> defineRegexInternal(String name, String defaultValue) {
        var config = new FabricRegexConfigValue(name, defaultValue, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<String> defineChoiceInternal(String name, String defaultValue, Predicate<Object> validator,
                                                    Supplier<List<String>> options, Function<String, ItemStack> icon) {
        var config = new FabricDropdownConfigValue(name, defaultValue, validator, options, icon, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        var config = new FabricListStringConfigValue<>(name, (List<String>) defaultValue, predicate, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<List<String>> defineListInternal(String name, List<String> defaultValue, Predicate<Object> entryValidator,
                                                        Supplier<List<String>> options, Function<String, ItemStack> icon) {
        var config = new FabricListStringConfigValue<>(name, defaultValue, entryValidator, options, icon, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        var config = new FabricEnumConfigValue<>(name, defaultValue, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<JsonElement> defineJson(String name, Supplier<JsonElement> defaultValue) {
        var config = new FabricJsonConfigValue(name, defaultValue, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<JsonElement> defineJson(String name, JsonElement defaultValue) {
        var config = new FabricJsonConfigValue(name, () -> defaultValue, pendingMeta());
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T> Supplier<T> defineObject(String name, Supplier<T> defaultValue, Codec<T> rawCodec) {
        // a SchemaCodec IS a Codec and writes the same thing, so wrapping it costs nothing and gets us a real form
        SchemaCodec<T> codec = SchemaCodec.wrap(rawCodec);
        var config = new FabricObjectConfigValue<>(name, defaultValue, codec, pendingMeta());
        doAddConfig(name, config, c -> new ConfigOption.SchemaOption<>(
                config.getTranslation(), null, config, config::getDefaultValue, codec));
        return config;
    }

}
