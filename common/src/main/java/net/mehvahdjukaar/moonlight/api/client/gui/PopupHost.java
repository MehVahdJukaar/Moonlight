package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.DropdownWidget;

/** A screen that can float Popups above its content through an OverlayLayer. */
public interface PopupHost {

    OverlayLayer getOverlayLayer();
}
