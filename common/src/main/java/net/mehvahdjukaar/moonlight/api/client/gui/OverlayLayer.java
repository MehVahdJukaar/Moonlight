package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * A screen-level layer that hosts a single floating {@link Popup} above the screen's normal content. Because a
 * popup's source widget can live inside a scissored list (which clips by rectangle, not depth, so a Z offset alone
 * can't escape it), the popup has to be drawn and input-tested at the screen level; this bundles that routing so a
 * host just forwards its events here instead of duplicating the plumbing.
 * <p>
 * A screen owns one of these (see {@link PopupHost}), forwards its {@code render}/mouse/key/char events to it
 * (giving it first refusal, since the popup floats above everything), and calls {@link #clear()} in {@code init()}
 * so a rebuilt widget tree never leaves a stale popup behind. Opening a new popup closes any previous one.
 */
public class OverlayLayer {

    @Nullable
    private Popup open;

    /** Makes {@code popup} the active one, closing whatever was open before. */
    public void open(Popup popup) {
        if (this.open != null && this.open != popup) {
            Popup previous = this.open;
            this.open = null;
            previous.onPopupClosed();
        }
        this.open = popup;
    }

    /** Closes {@code popup} if it is the one currently open (a no-op otherwise). */
    public void close(Popup popup) {
        if (this.open == popup) {
            this.open = null;
            popup.onPopupClosed();
        }
    }

    /** Drops any open popup (e.g. from a host's {@code init()} before it rebuilds its widgets). */
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

    /** A consumed click, including outside clicks that dismiss the popup. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return open != null && open.popupMouseClicked(mouseX, mouseY, button);
    }

    /** While open the wheel is modal: it scrolls the popup and is always swallowed so the content behind stays put. */
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (open == null) return false;
        open.popupMouseScrolled(mouseX, mouseY, amount);
        return true;
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return open != null && open.popupKeyPressed(key, scanCode, modifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        return open != null && open.popupCharTyped(c, modifiers);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (open != null) open.renderPopup(graphics, mouseX, mouseY);
    }
}
