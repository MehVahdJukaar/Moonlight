package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface Popup {

    /** Draw the popup. Called by the host after everything else, so it floats on top. */
    void renderPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

    /** @return true if this click was consumed (including outside clicks that should dismiss the popup). */
    default boolean popupMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    default boolean popupMouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    default boolean popupKeyPressed(KeyEvent event) {
        return false;
    }

    default boolean popupCharTyped(CharacterEvent event) {
        return false;
    }

    /** Called when the layer stops tracking this popup (replaced, cleared, or explicitly closed). */
    default void onPopupClosed() {
    }
}
