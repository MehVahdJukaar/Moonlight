package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.DropdownWidget;

/**
 * A screen that can float {@link Popup}s above its content. It owns an {@link OverlayLayer}, forwards its
 * render/mouse/key/char events to it first so popups sit on top, and clears it in {@code init()}. Widgets that need
 * to escape a scissored parent find the host by instance-checking the current screen.
 */
public interface PopupHost {

    OverlayLayer getOverlayLayer();
}
