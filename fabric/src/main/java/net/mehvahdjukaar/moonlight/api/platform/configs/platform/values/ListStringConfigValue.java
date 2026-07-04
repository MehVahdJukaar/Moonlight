package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListStringConfigValue<T extends String> extends ConfigValue<List<String>> {

    private final Predicate<Object> predicate;
    // non-null -> entries are picked from a dropdown instead of typed (see the screen's list editor)
    @Nullable
    private final Supplier<List<String>> options;
    @Nullable
    private final Function<String, ItemStack> icon;

    public ListStringConfigValue(String name, List<String> defaultValue, Predicate<Object> validator, ConfigMeta meta) {
        this(name, defaultValue, validator, null, null, meta);
    }

    public ListStringConfigValue(String name, List<String> defaultValue, Predicate<Object> validator,
                                 @Nullable Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon, ConfigMeta meta) {
        super(name, defaultValue, meta);
        this.predicate = validator;
        this.options = options;
        this.icon = icon;
    }

    public Predicate<Object> getPredicate() {
        return predicate;
    }

    @Nullable
    public Supplier<List<String>> getOptions() {
        return options;
    }

    @Nullable
    public Function<String, ItemStack> getIcon() {
        return icon;
    }

    @Override
    public boolean isValid(List<String> value) {
        return true;
    }

    @Override
    protected List<String> parseValue(JsonElement element) {
        if (!(element instanceof JsonArray ja)) return null; // wrong shape -> falls back to the default
        List<String> newValue = new ArrayList<>();
        for (var v : ja) {
            String s = v.getAsString();
            if (this.predicate.test(s)) newValue.add(s);
        }
        return newValue;
    }

    @Override
    protected JsonElement encodeValue(List<String> value) {
        JsonArray ja = new JsonArray();
        value.forEach(ja::add);
        return ja;
    }
}
