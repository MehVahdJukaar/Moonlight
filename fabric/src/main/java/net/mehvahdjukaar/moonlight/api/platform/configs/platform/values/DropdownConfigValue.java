package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * String value picked from a list or a registry, shown as a dropdown.
 */
public class DropdownConfigValue extends StringConfigValue {

    private final Supplier<List<String>> options;
    @Nullable
    private final Function<String, ItemStack> icon;

    public DropdownConfigValue(String name, String defaultValue, Predicate<Object> validator,
                               Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon, ConfigMetadata meta) {
        super(name, defaultValue, validator, meta);
        this.options = options;
        this.icon = icon;
    }

    public Supplier<List<String>> getOptions() {
        return options;
    }

    @Nullable
    public Function<String, ItemStack> getIcon() {
        return icon;
    }
}
