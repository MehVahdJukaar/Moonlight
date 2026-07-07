package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.screens.Screen;

import java.util.*;

/**
 * Mutable editing state for one config screen visit, shared across the whole navigation stack (so edits and
 * expanded rows made in a subcategory survive going back and are persisted by a single Save). Holds only a
 * working copy; nothing is written to the underlying config until {@link #apply()}.
 */
public final class ConfigEditSession {

    private final ModConfigHolder holder;
    private final Screen returnScreen;

    private final Map<ConfigOption<?>, Object> pending = new IdentityHashMap<>();
    private final Set<ConfigOption<?>> expanded = Collections.newSetFromMap(new IdentityHashMap<>());
    // the most severe reload requirement among values actually saved this visit (sticky across multiple saves)
    private ConfigReloadType appliedReload = ConfigReloadType.NONE;

    public ConfigEditSession(ModConfigHolder holder, Screen returnScreen) {
        this.holder = holder;
        this.returnScreen = returnScreen;
    }

    public ModConfigHolder holder() {
        return holder;
    }

    public Screen returnScreen() {
        return returnScreen;
    }

    /**
     * The value to display: the pending edit if there is one, otherwise the saved value.
     */
    @SuppressWarnings("unchecked")
    public <T> T current(ConfigOption<T> v) {
        return pending.containsKey(v) ? (T) pending.get(v) : v.get();
    }

    public Object currentRaw(ConfigOption<?> v) {
        return pending.containsKey(v) ? pending.get(v) : v.get();
    }

    public void put(ConfigOption<?> v, Object value) {
        pending.put(v, value);
    }

    public int unsavedCount() {
        int count = 0;
        for (Map.Entry<ConfigOption<?>, Object> e : pending.entrySet()) {
            if (!Objects.equals(e.getValue(), e.getKey().get())) count++;
        }
        return count;
    }

    public void apply() {
        pending.forEach((v, value) -> {
            if (!Objects.equals(value, v.get())) {
                v.apply(holder, value);
                // remember the heaviest reload a saved change needs, so the exit can prompt for it
                if (v.reloadType().ordinal() > appliedReload.ordinal()) appliedReload = v.reloadType();
            }
        });
    }

    /**
     * Most severe reload a saved change has required this visit ({@link ConfigReloadType#NONE} if none).
     */
    public ConfigReloadType appliedReload() {
        return appliedReload;
    }

    public void clearPending() {
        pending.clear();
    }

    public boolean isExpanded(ConfigOption<?> v) {
        return expanded.contains(v);
    }

    public void toggleExpanded(ConfigOption<?> v) {
        if (!expanded.remove(v)) expanded.add(v);
    }
}
