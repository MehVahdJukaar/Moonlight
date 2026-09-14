package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;

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
