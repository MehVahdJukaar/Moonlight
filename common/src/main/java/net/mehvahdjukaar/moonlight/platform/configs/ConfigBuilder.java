package net.mehvahdjukaar.moonlight.platform.configs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ConfigBuilder {

    @ExpectPlatform
    public static ConfigBuilder create(String name, ConfigBuilder.ConfigType type) {
        throw new AssertionError();
    }


    private final String name;
    protected final ConfigType type;

    public ConfigBuilder(String name, ConfigType type) {
        this.name = name;
        this.type = type;
    }

    public enum ConfigType {
        CLIENT, COMMON;

        public String getFileName(){
            return Moonlight.MOD_ID+"-"+this.toString().toLowerCase(Locale.ROOT)+".json";
        }
    }

    public abstract void buildAndRegister();

    public String getName() {
        return name;
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

    public abstract Supplier<String> define(String name, String defaultValue);

    public Supplier<List<String>> define(String name, List<String> defaultValue){
        return define(name, defaultValue, s->true);
    };

    public abstract <T extends String>  Supplier<List<T>> define(String name, List<T> defaultValue, Predicate<T> predicate);

    public abstract <V extends Enum<V>> Supplier<V> define(String name, V defaultValue);


    public static TranslatableComponent descriptionKey(String name) {
        return new TranslatableComponent("text.immersive_weathering." + name);
    }

    public static TranslatableComponent tooltipKey(String name) {
        return new TranslatableComponent("text.immersive_weathering." + name + ".descriptionKey");
    }
}
