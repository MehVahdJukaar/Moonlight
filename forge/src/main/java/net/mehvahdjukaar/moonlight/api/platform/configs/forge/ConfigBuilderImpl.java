package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    private final List<ForgeConfigSpec.ConfigValue<?>> requireGameRestart = new ArrayList<>();
    private boolean currentGameRestart;
    private ForgeConfigSpec.ConfigValue<?> currentValue;

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
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
    public ConfigSpecWrapper build() {
        return new ConfigSpecWrapper(this.getName(), this.builder.build(), this.type, this.synced,
                this.changeCallback, this.requireGameRestart);
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
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue) {
        maybeAddTranslationString(name);
        var stringConfig = builder.define(name, Integer.toHexString(defaultValue), ConfigBuilder.COLOR_CHECK);
        this.currentValue = stringConfig;
        maybeAddGameRestart();
        return () -> Integer.parseUnsignedInt(stringConfig.get().replace("0x", ""), 16);
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue, validator);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        maybeAddTranslationString(name);
        var value = builder.defineList(name, defaultValue, predicate);
        this.currentValue = value;
        maybeAddGameRestart();
        return () -> (List<String>) value.get();
    }

    @Override
    public <T> Supplier<List<? extends T>> defineForgeList(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        maybeAddTranslationString(name);
        var value = builder.defineList(name, defaultValue, predicate);
        return (Supplier<List<? extends T>>) value;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        maybeAddTranslationString(name);
        var value = builder.defineEnum(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    private void maybeAddGameRestart() {
        if(currentGameRestart && currentValue != null){
            requireGameRestart.add(currentValue);
            currentGameRestart = false;
            currentValue = null;
        }
    }

    @Override
    public ConfigBuilder gameRestart() {
        this.currentGameRestart = true;
        return this;
    }

    @Override
    public ConfigBuilder worldReload(){
        builder.worldRestart();
        return this;
    }

    @Override
    public ConfigBuilder comment(String comment) {
        builder.comment(comment); //.translationKey(getTranslationName());
        //TODO: choose. either add a translation or a comment literal not both
        return super.comment(comment);
    }
}
