package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

/**
 * A bound editing widget produced by a {@link ConfigControls.Provider}: the widget itself plus a hook to push a
 * value into it (used by the rollback button to display the default again without going through user input).
 */
public record Control(AbstractWidget widget, Consumer<Object> setDisplayed) {
}
