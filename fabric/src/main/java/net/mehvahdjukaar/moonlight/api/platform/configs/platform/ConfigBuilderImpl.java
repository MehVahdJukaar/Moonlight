package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.values.*;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.http.annotation.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ConfigSubCategory mainCategory = new ConfigSubCategory(this.getName().getNamespace());

    private final Deque<ConfigSubCategory> categoryStack = new ArrayDeque<>();

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        categoryStack.push(mainCategory);
    }

    //doesn't load it immediately. happens after registration to mimic forge
    @NotNull
    public FabricConfigHolder build() {
        assert categoryStack.size() == 1;
        return new FabricConfigHolder(this.getName(), mainCategory, this.type, this.buildChangeCallback(), getUiRoot());
    }

    @Override
    public String currentCategory() {
        return Objects.requireNonNull(categoryStack.peek()).getName();
    }

    @Nullable
    @Override
    public String parentCategory() {
        if (categoryStack.size() < 2) {
            return null;
        }
        var it = categoryStack.descendingIterator();
        it.next(); // current category
        return it.next().getName(); // parent category
    }

    @Override
    public ConfigBuilderImpl push(String translation) {
        var cat = new ConfigSubCategory(translation);
        Objects.requireNonNull(categoryStack.peek()).addEntry(cat);
        categoryStack.push(cat);
        // register a readable name for the category so the native config screen button isn't a raw key
        translations.put(translationKey(""), LangBuilder.getReadableName(translation));
        uiPush(Component.translatable(translationKey("")));
        return this;
    }


    @Override
    public ConfigBuilderImpl pop() {
        assert categoryStack.size() != 1;
        categoryStack.pop();
        uiPop();
        return this;
    }

    private void doAddConfig(String name, ConfigValue<?> config) {
        config.setTranslationKey(this.translationKey(name));
        addTranslationsAndComments(name);

        config.setAffectsDynamicPacks(config.affectsDynamicPacks() || this.pendingDynamicPacks);
        this.pendingDynamicPacks = false;
        // world-reload / game-restart is carried on the option node itself now (see ConfigBuilder#recordOption)
        Objects.requireNonNull(this.categoryStack.peek()).addEntry(config);
        if (this.categoryStack.size() <= 1 && PlatHelper.isDev()) throw new AssertionError();

        // build the matching screen row; the comment (before or after) fills in its description and file comment
        if (!suppressUi) {
            ConfigOption<?> option = toOption(config);
            recordOption(option);
            noteDefined(name, option, raw -> {
                config.setRawComment(raw);
                config.setCommentKey(this.tooltipKey(name));
            });
        }
    }

    /** Translates a stored value into the matching loader independent screen row. Description is left empty here;
     * {@code comment(...)} fills it in later (before or after the define) via {@code noteDefined}. */
    private static ConfigOption<?> toOption(ConfigValue<?> v) {
        Component title = v.getTranslation();
        boolean slider = v.isSlider();
        // ColorConfigValue extends IntConfigValue, so it must be checked first
        if (v instanceof ColorConfigValue c) {
            return new ConfigOption.ColorValue(title, null, c, c.getDefaultValue());
        } else if (v instanceof IntConfigValue i) {
            return slider
                    ? new ConfigOption.IntSliderValue(title, null, i, i.getDefaultValue(), i.getMin(), i.getMax())
                    : new ConfigOption.IntValue(title, null, i, i.getDefaultValue(), i.getMin(), i.getMax());
        } else if (v instanceof DoubleConfigValue d) {
            if (v.isPercent()) return new ConfigOption.PercentValue(title, null, d, d.getDefaultValue());
            return slider
                    ? new ConfigOption.DoubleSliderValue(title, null, d, d.getDefaultValue(), d.getMin(), d.getMax())
                    : new ConfigOption.DoubleValue(title, null, d, d.getDefaultValue(), d.getMin(), d.getMax());
        } else if (v instanceof FloatConfigValue f) {
            return slider
                    ? new ConfigOption.FloatSliderValue(title, null, f, f.getDefaultValue(), f.getMin(), f.getMax())
                    : new ConfigOption.FloatValue(title, null, f, f.getDefaultValue(), f.getMin(), f.getMax());
        } else if (v instanceof BoolConfigValue b) {
            return new ConfigOption.BooleanValue(title, null, b, b.getDefaultValue());
        } else if (v instanceof EnumConfigValue<?> e) {
            return enumOption(e, title);
        } else if (v instanceof RegexConfigValue r) {
            // must be checked before StringConfigValue (RegexConfigValue extends it)
            return new ConfigOption.RegexValue(title, null, r, r.getDefaultValue());
        } else if (v instanceof DropdownConfigValue d) {
            // also extends StringConfigValue, so check first
            return new ConfigOption.DropdownValue(title, null, d, d.getDefaultValue(), d.getOptions(), d.getIcon());
        } else if (v instanceof StringConfigValue s) {
            return new ConfigOption.StringValue(title, null, s, s.getDefaultValue(),
                    o -> o instanceof String str && s.isValid(str));
        } else if (v instanceof ListStringConfigValue<?> l) {
            @SuppressWarnings("unchecked")
            Supplier<List<String>> handle = (Supplier<List<String>>) l;
            return new ConfigOption.ListValue(title, null, handle, l.getDefaultValue(),
                    l.getPredicate()::test, l.getOptions(), l.getIcon());
        } else if (v instanceof JsonConfigValue j) {
            // raw json + beans (defineBean rides on defineJson) -> editable json text box
            return new ConfigOption.JsonValue(title, null, j);
        }
        // codec objects: not yet editable
        @SuppressWarnings("unchecked")
        Supplier<Object> handle = (Supplier<Object>) v;
        return new ConfigOption.UnsupportedValue(title, null, handle);
    }

    private static <E extends Enum<E>> ConfigOption.EnumValue<E> enumOption(EnumConfigValue<E> e, Component title) {
        return new ConfigOption.EnumValue<>(title, null, e, e.getDefaultValue(), e.getEnumClass().getEnumConstants());
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        var config = new BoolConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }


    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        var config = new DoubleConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        var config = new FloatConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Double> definePercentage(String name, double defaultValue) {
        var config = new DoubleConfigValue(name, defaultValue, 0.0, 1.0);
        config.setPercent(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        var config = new IntConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> defineSlider(String name, int defaultValue, int min, int max) {
        var config = new IntConfigValue(name, defaultValue, min, max);
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Double> defineSlider(String name, double defaultValue, double min, double max) {
        var config = new DoubleConfigValue(name, defaultValue, min, max);
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Float> defineSlider(String name, float defaultValue, float min, float max) {
        var config = new FloatConfigValue(name, defaultValue, min, max);
        config.setSlider(true);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue) {
        var config = new ColorConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        var config = new StringConfigValue(name, defaultValue, validator);
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<String> defineRegexSource(String name, String defaultValue) {
        var config = new RegexConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<String> defineChoiceSource(String name, String defaultValue, Predicate<Object> validator,
                                                  Supplier<List<String>> options, Function<String, ItemStack> icon) {
        var config = new DropdownConfigValue(name, defaultValue, validator, options, icon);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        var config = new ListStringConfigValue<>(name, (List<String>) defaultValue, predicate);
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected Supplier<List<String>> defineListSource(String name, List<String> defaultValue, Predicate<Object> entryValidator,
                                                      Supplier<List<String>> options, Function<String, ItemStack> icon) {
        var config = new ListStringConfigValue<>(name, defaultValue, entryValidator, options, icon);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        var config = new EnumConfigValue<>(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<JsonElement> defineJson(String name, Supplier<JsonElement> defaultValue) {
        var config = new JsonConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<JsonElement> defineJson(String name, JsonElement defaultValue) {
        var config = new JsonConfigValue(name, () -> defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultValue, Codec<T> codec) {
        var config = new ObjectConfigValue<>(name, defaultValue, codec);
        doAddConfig(name, config);
        return config;
    }

}
