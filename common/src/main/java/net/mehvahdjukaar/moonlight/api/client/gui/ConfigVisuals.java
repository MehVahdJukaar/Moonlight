package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigControllers;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public record ConfigVisuals<T>(AbstractWidget widget, Consumer<T> valueSetter) {

    @SuppressWarnings("unchecked")
    public void set(Object value) {
        valueSetter.accept((T) value);
    }

    @FunctionalInterface
    public interface Provider<O extends ConfigOption<?>> {
        ConfigVisuals<?> create(O option, ConfigEditSession session, Runnable onChange);
    }

    /** Registers the control provider used to edit a given kind of config option. */
    public static <O extends ConfigOption<?>> void register(Class<O> type, ConfigVisuals.Provider<O> provider) {
        ConfigControllers.register(type, provider);
    }
}
