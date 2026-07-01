package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListStringConfigValue<T extends String>  extends ConfigValue<List<String>> {

    private final Predicate<Object> predicate;
    // non-null -> entries are picked from a dropdown instead of typed (see the screen's list editor)
    @Nullable
    private final Supplier<List<String>> options;
    @Nullable
    private final Function<String, ItemStack> icon;

    public ListStringConfigValue(String name, List<String> defaultValue, Predicate<Object> validator) {
        this(name, defaultValue, validator, null, null);
    }

    public ListStringConfigValue(String name, List<String> defaultValue, Predicate<Object> validator,
                                 @Nullable Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon) {
        super(name, defaultValue);
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
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                var array = element.get(this.name);
                if(array instanceof JsonArray ja){
                    List<String> newValue = new ArrayList<>();
                    for(var v : ja){
                        T s = (T) v.getAsString();
                        if(this.predicate.test(s)) newValue.add(s);
                    }
                    boolean changed = this.setAndTrack(newValue);
                    this.markLoaded();
                    return this.affectsDynamicPacks() && changed;
                }
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
        this.markLoaded();
        return false;
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        JsonArray ja = new JsonArray();
        this.value.forEach(ja::add);
        object.add(this.name, ja);
    }


}
