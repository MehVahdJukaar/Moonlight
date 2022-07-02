package net.mehvahdjukaar.moonlight.platform.configs.forge;

import net.mehvahdjukaar.moonlight.platform.configs.ConfigBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(String name, ConfigBuilder.ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ForgeConfigSpec.Builder builder;

    public ConfigBuilderImpl(String name, ConfigType type) {
        super(name, type);
        this.builder = new ForgeConfigSpec.Builder();
    }

    @Override
    public void buildAndRegister() {
        ModConfig.Type t = this.type == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;
        ModLoadingContext.get().registerConfig(t, this.builder.build());
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        builder.push(category);
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        builder.pop();
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        var value = builder.translation(tooltipKey(name).getKey()).define(name, defaultValue);
        return value::get;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        var value = builder.translation(tooltipKey(name).getKey()).defineInRange(name, defaultValue, min, max);
        return value::get;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        var value = builder.translation(tooltipKey(name).getKey()).defineInRange(name, defaultValue, min, max);
        return value::get;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue) {
        ForgeConfigSpec.ConfigValue<String> value = builder.translation(tooltipKey(name).getKey()).define(name, defaultValue);
        return value::get;
    }

    @Override
    public <T extends String> Supplier<List<T>> define(String name, List<T> defaultValue, Predicate<T> predicate) {
           var value = builder.translation(tooltipKey(name).getKey()).defineList(name, defaultValue,
                   o -> predicate.test((T) o));
            return ()-> (List<T>) value.get();

    }


    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        var value = builder.translation(tooltipKey(name).getKey()).defineEnum(name, defaultValue);
        return value::get;
    }
}
