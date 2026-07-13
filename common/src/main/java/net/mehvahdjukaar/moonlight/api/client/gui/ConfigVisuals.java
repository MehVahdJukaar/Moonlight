package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigControllers;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

/**
 * A bound editing widget: the widget itself plus a delegate that pushes a value into it (used, for example, to
 * redisplay a default without going through user input). Produced by a config control provider.
 */
public record ConfigVisuals<T>(AbstractWidget widget, Consumer<T> valueSetter) {

    /**
     * Pushes a value into the widget when {@code T} isn't known statically — the config option row holds these as
     * {@code WidgetAndSetter<?>} (its registry is keyed by option class), so it can't call {@link #valueSetter()}
     * directly. The value is trusted to match the widget, which the registry guarantees.
     */
    @SuppressWarnings("unchecked")
    public void set(Object value) {
        valueSetter.accept((T) value);
    }

    @FunctionalInterface
    public interface Provider<O extends ConfigOption<?>> {
        ConfigVisuals<?> create(O option, ConfigEditSession session, Runnable onChange);
    }

    /**
     * Registers a configuration control provider for a specific type of configuration option.
     *
     * @param <O>       the type of configuration option this provider applies to, extending {@code ConfigOption<?>}.
     * @param type      the class type of the configuration option that this provider supports.
     * @param provider  the implementation of the {@code ConfigControl.Provider} for the specified type.
     */
    public static <O extends ConfigOption<?>> void register(Class<O> type, ConfigVisuals.Provider<O> provider) {
        ConfigControllers.register(type, provider);
    }
}
