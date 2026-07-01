package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mutable editing state for one config screen visit, shared across the whole navigation stack (so edits and
 * expanded rows made in a sub category survive going back and are persisted by a single Save). Holds only a
 * working copy; nothing is written to the underlying config until {@link #apply()}.
 */
public final class ConfigEditSession {

    private final ModConfigHolder holder;
    private final Screen returnScreen;
    @Nullable
    private final ResourceLocation background;

    private final Map<ConfigOption<?>, Object> pending = new IdentityHashMap<>();
    private final Set<ConfigOption<?>> expanded = Collections.newSetFromMap(new IdentityHashMap<>());

    ConfigEditSession(ModConfigHolder holder, Screen returnScreen, @Nullable ResourceLocation background) {
        this.holder = holder;
        this.returnScreen = returnScreen;
        this.background = background;
    }

    public ModConfigHolder holder() {
        return holder;
    }

    Screen returnScreen() {
        return returnScreen;
    }

    @Nullable
    ResourceLocation background() {
        return background;
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

    /**
     * Number of edits that actually differ from the saved value (toggling back to the saved state doesn't count).
     */
    int unsavedCount() {
        int count = 0;
        for (Map.Entry<ConfigOption<?>, Object> e : pending.entrySet()) {
            if (!Objects.equals(e.getValue(), e.getKey().get())) count++;
        }
        return count;
    }

    void apply() {
        pending.forEach((v, value) -> {
            if (!Objects.equals(value, v.get())) v.apply(holder, value);
        });
    }

    void clearPending() {
        pending.clear();
    }

    boolean isExpanded(ConfigOption<?> v) {
        return expanded.contains(v);
    }

    void toggleExpanded(ConfigOption<?> v) {
        if (!expanded.remove(v)) expanded.add(v);
    }
}
