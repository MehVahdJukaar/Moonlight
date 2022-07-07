package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.language.LangBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(ResourceLocation name, ConfigBuilder.ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }


    private final ImmutableList.Builder<ConfigCategory> categoriesBuilder = new ImmutableList.Builder<>();
    private Pair<String, ImmutableList.Builder<ConfigEntry>> currentCategoryBuilder;

    public ConfigBuilderImpl(ResourceLocation name, ConfigBuilder.ConfigType type) {
        super(name, type);
    }

    @Override
    public ConfigSpec buildAndRegister() {
        ConfigSpec spec = build();
        if (type == ConfigType.COMMON) {
            ConfigSpec.COMMON_INSTANCE = spec;
        } else {
            ConfigSpec.CLIENT_INSTANCE = spec;
        }
        return spec;
    }

    @NotNull
    public ConfigSpec build() {
        assert currentCategoryBuilder == null;
        ConfigSpec spec = new ConfigSpec(new ResourceLocation(this.getModId(), this.getName()),
                categoriesBuilder.build(), this.getFileName());
        spec.loadConfig();
        spec.saveConfig();
        return spec;
    }

    @Override
    protected String currentCategory() {
        return currentCategoryBuilder.getFirst();
    }

    @Override
    public ConfigBuilderImpl push(String translation) {
        assert currentCategoryBuilder == null;
        currentCategoryBuilder = Pair.of(translation, new ImmutableList.Builder<>());
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        assert currentCategoryBuilder != null;
        categoriesBuilder.add(new ConfigCategory(currentCategoryBuilder.getFirst(), currentCategoryBuilder.getSecond().build()));
        this.currentCategoryBuilder = null;
        return this;
    }

    private void doAddConfig(String name, ConfigValue<?> config) {
        config.setDescriptionKey(this.tooltipKey(name));
        config.setTranslationKey(this.translationKey(name));
        maybeAddComment(name);
        this.currentCategoryBuilder.getSecond().add(config);
    }


    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        assert currentCategoryBuilder != null;
        var config = new BoolConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }


    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        assert currentCategoryBuilder != null;
        var config = new DoubleConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        assert currentCategoryBuilder != null;
        var config = new IntConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        assert currentCategoryBuilder != null;
        var config = new StringConfigValue(name, defaultValue, validator);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate){
        assert currentCategoryBuilder != null;
        var config = new ListStringConfigValue<>(name, (List<String>) defaultValue, predicate);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        assert currentCategoryBuilder != null;
        var config = new EnumConfigValue<>(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }

    @Override
    protected void maybeAddComment(String name) {
        comments.put(this.translationKey(name), LangBuilder.getReadableName(name));
        super.maybeAddComment(name);
    }
}
