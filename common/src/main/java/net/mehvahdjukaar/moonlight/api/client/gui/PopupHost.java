package net.mehvahdjukaar.moonlight.api.client.gui;

/**
 * A screen that can float {@link Popup}s above its content. It owns an {@link OverlayLayer}, forwards its
 * render/mouse/key/char events to it (first, so popups sit on top), and clears it in {@code init()}.
 * <p>
 * Widgets that need to escape a scissored parent (e.g. {@link DropdownWidget}) discover the host via
 * {@code Minecraft.getInstance().screen instanceof PopupHost} and open themselves through {@link #getOverlayLayer()}.
 */
public interface PopupHost {

    OverlayLayer getOverlayLayer();
}
