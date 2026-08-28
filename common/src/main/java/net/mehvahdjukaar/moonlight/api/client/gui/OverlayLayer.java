package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Holds one floating Popup drawn above the screen's normal content. The widget that opened it often lives inside a
 * scissored list, and scissor clips by rectangle and not by depth, so a Z offset isn't enough to get out of it: the
 * popup has to be drawn and clicked at screen level. PopupHost is what a screen has to implement for that to work.
 * Opening a new popup closes the previous one.
 */
public class OverlayLayer {

    @Nullable
    private Popup open;

    /** Makes this the active popup, closing whatever was open before. */
    public void open(Popup popup) {
        if (this.open != null && this.open != popup) {
            Popup previous = this.open;
            this.open = null;
            previous.onPopupClosed();
        }
        this.open = popup;
    }

    /** Closes the given popup if it is the one currently open. */
    public void close(Popup popup) {
        if (this.open == popup) {
            this.open = null;
            popup.onPopupClosed();
        }
    }

    /** Drops any open popup, as a host must do before rebuilding its widgets. */
    public void clear() {
        if (this.open != null) {
            Popup previous = this.open;
            this.open = null;
            previous.onPopupClosed();
        }
    }

    public boolean isOpen() {
        return open != null;
    }

    // outside clicks count as consumed too, since they dismiss the popup
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return open != null && open.popupMouseClicked(event, doubleClick);
    }

    // while open the wheel is modal: it scrolls the popup and is swallowed so the content behind stays put
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (open == null) return false;
        open.popupMouseScrolled(mouseX, mouseY, amount);
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        return open != null && open.popupKeyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return open != null && open.popupCharTyped(event);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (open != null) open.renderPopup(graphics, mouseX, mouseY);
    }
}
