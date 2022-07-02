package net.mehvahdjukaar.moonlight.platform.configs.fabric;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.platform.configs.fabric.values.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(String name, ConfigBuilder.ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ImmutableList.Builder<ConfigCategory> categoriesBuilder = new ImmutableList.Builder<>();
    private Pair<String, ImmutableList.Builder<ConfigEntry>> currentCategoryBuilder;

    public ConfigBuilderImpl(String name, ConfigBuilder.ConfigType type) {
        super(name, type);
    }

    @Override
    public void buildAndRegister() {
        assert currentCategoryBuilder == null;
        ConfigSpec spec = new ConfigSpec(this.getName(), categoriesBuilder.build(), type.getFileName());
        spec.loadConfig();
        spec.saveConfig();
        if (type == ConfigType.COMMON) {
            ConfigSpec.COMMON_INSTANCE = spec;
        } else {
            ConfigSpec.CLIENT_INSTANCE = spec;
        }
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

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        assert currentCategoryBuilder != null;
        var config = new BoolConfigValue(name, defaultValue);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        assert currentCategoryBuilder != null;
        var config = new DoubleConfigValue(name, defaultValue, min, max);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        assert currentCategoryBuilder != null;
        var config = new IntConfigValue(name, defaultValue, min, max);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue) {
        assert currentCategoryBuilder != null;
        var config = new StringConfigValue(name, defaultValue);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

    @Override
    public <T extends String> Supplier<List<T>> define(String name, List<T> defaultValue, Predicate<T> predicate) {
        assert currentCategoryBuilder != null;
        var config = new ListStringConfigValue<>(name,defaultValue, predicate);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        assert currentCategoryBuilder != null;
        var config = new EnumConfigValue<>(name, defaultValue);
        this.currentCategoryBuilder.getSecond().add(config);
        return config;
    }

}
