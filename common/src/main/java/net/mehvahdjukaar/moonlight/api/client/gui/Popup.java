package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A floating element that a {@link PopupHost} screen draws and input-tests <em>after</em> (and above) its normal
 * content, via an {@link OverlayLayer}. This is the escape hatch for content that must not be clipped by a
 * scissored/scrolling parent (a combo box list, a context menu, an autocomplete box, ...): a widget nested in such
 * a container hands a {@code Popup} to the host so it can render at screen level, outside any scissor.
 * <p>
 * All input hooks return whether they consumed the event; the {@code render} hook draws the popup (typically at a
 * raised Z so it sits above sibling content). {@link #onPopupClosed()} lets the popup reset its own state when the
 * layer drops it (host {@code init}, or another popup opening).
 */
public interface Popup {

    /** Draw the popup. Called by the host after everything else, so it floats on top. */
    void renderPopup(GuiGraphics graphics, int mouseX, int mouseY);

    /** @return true if this click was consumed (including outside clicks that should dismiss the popup). */
    default boolean popupMouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean popupMouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    default boolean popupKeyPressed(int key, int scanCode, int modifiers) {
        return false;
    }

    default boolean popupCharTyped(char c, int modifiers) {
        return false;
    }

    /** Called when the layer stops tracking this popup (replaced, cleared, or explicitly closed). */
    default void onPopupClosed() {
    }
}
