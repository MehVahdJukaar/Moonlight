package net.mehvahdjukaar.moonlight.platform.configs.forge;

import net.mehvahdjukaar.moonlight.platform.configs.ConfigBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(ResourceLocation name, ConfigBuilder.ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ForgeConfigSpec.Builder builder;

    private String cat = null;

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        this.builder = new ForgeConfigSpec.Builder();
    }

    @Override
    protected String currentCategory() {
        return cat;
    }

    @Override
    public ForgeConfigSpec buildAndRegister() {
        ModConfig.Type t = this.type == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;
        ForgeConfigSpec spec = build();
        ModLoadingContext.get().registerConfig(t, spec);
        return spec;
    }

    @Override
    public ForgeConfigSpec build() {
        return this.builder.build();
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        assert cat == null;
        builder.push(category);
        cat = category;
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        assert cat != null;
        builder.pop();
        cat = null;
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        maybeAddComment(this.tooltipKey(name));
        return builder.translation(tooltipKey(name)).define(name, defaultValue);
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        maybeAddComment(this.tooltipKey(name));
        return builder.translation(tooltipKey(name)).defineInRange(name, defaultValue, min, max);
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        maybeAddComment(this.tooltipKey(name));
        return builder.translation(tooltipKey(name)).defineInRange(name, defaultValue, min, max);
    }

    @Override
    public Supplier<String> define(String name, String defaultValue) {
        maybeAddComment(this.tooltipKey(name));
        return builder.translation(tooltipKey(name)).define(name, defaultValue);
    }

    @Override
    public <T extends String> Supplier<List<T>> define(String name, List<T> defaultValue, Predicate<T> predicate) {
        maybeAddComment(this.tooltipKey(name));
           var value = builder.translation(tooltipKey(name)).defineList(name, defaultValue,
                   o -> predicate.test((T) o));
            return ()-> (List<T>) value.get();
    }


    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        maybeAddComment(this.tooltipKey(name));
        return builder.translation(tooltipKey(name)).defineEnum(name, defaultValue);
    }
}
