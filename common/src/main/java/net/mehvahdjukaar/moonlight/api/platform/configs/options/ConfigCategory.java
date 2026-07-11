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

    /**
     * The optional "feature" boolean that enables/disables this whole category (see
     * {@code ConfigBuilder.feature}). When null the category is always active. Its value only <em>gates</em> the
     * category at read time (through composed suppliers); it never rewrites the child entries.
     */
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

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
