package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.DropdownWidget;

/**
 * A screen that can draw Popups above its own content. It owns an OverlayLayer, sends its render/mouse/key/char
 * events there first so popups stay on top, and clears it in init(). Widgets that have to get out of a scissored
 * parent find the host by instanceof-ing the current screen.
 */
public interface PopupHost {

    OverlayLayer getOverlayLayer();
}
