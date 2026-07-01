package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A navigable group of config entries. Rendered as one big button that opens a sub screen listing its
 * {@link #entries()}. The root of a config is also a category (its title is the config file name).
 */
public class ConfigCategory extends ConfigNode {

    private final List<ConfigNode> entries = new ArrayList<>();

    public ConfigCategory(Component title, @Nullable Component description) {
        super(title, description);
    }

    public ConfigCategory(Component title) {
        this(title, null);
    }

    public void add(ConfigNode entry) {
        this.entries.add(entry);
    }

    public List<ConfigNode> entries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
