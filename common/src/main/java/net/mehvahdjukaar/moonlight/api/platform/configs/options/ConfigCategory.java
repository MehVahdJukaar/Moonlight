package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategory extends ConfigNode {

    private final List<ConfigNode> entries = new ArrayList<>();
    @Nullable
    private ConfigOption.BooleanValue gate;

    public ConfigCategory(Component title, @Nullable Component description) {
        super(title, description);
    }

    public ConfigCategory(Component title) {
        this(title, null);
    }

    public void add(ConfigNode entry) {
        this.entries.add(entry);
        entry.setParent(this);
    }

    @Nullable
    public ConfigOption.BooleanValue gate() {
        return gate;
    }

    public void setGate(ConfigOption.BooleanValue gate) {
        this.gate = gate;
    }

    public List<ConfigNode> entries() {
        return entries;
    }

    public void clear() {
        this.entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
