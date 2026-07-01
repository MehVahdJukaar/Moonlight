package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A string config value picked from a set of options (a plain list, or a registry). Stored like a
 * {@link StringConfigValue}; the distinct type carries the lazy option list and optional icon and tells the screen
 * to use the dropdown widget.
 */
public class DropdownConfigValue extends StringConfigValue {

    private final Supplier<List<String>> options;
    @Nullable
    private final Function<String, ItemStack> icon;

    public DropdownConfigValue(String name, String defaultValue, Predicate<Object> validator,
                               Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon) {
        super(name, defaultValue, validator);
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
