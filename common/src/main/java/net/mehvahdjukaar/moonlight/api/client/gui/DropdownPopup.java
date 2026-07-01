package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * Per-screen holder for the single open {@link DropdownWidget} popup. Because a dropdown can live inside a scissored
 * list, its expanded popup has to be drawn and input-tested at the screen level so it can float above everything;
 * this bundles that render + input routing so any {@link DropdownWidget.Host} screen just forwards its events here
 * instead of duplicating the plumbing.
 */
public class DropdownPopup {

    @Nullable
    private DropdownWidget open;

    public void set(@Nullable DropdownWidget dropdown) {
        this.open = dropdown;
    }

    public void reset() {
        this.open = null;
    }

    public boolean isOpen() {
        return open != null;
    }

    /** A consumed click, including outside clicks that dismiss the popup. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return open != null && open.popupMouseClicked(mouseX, mouseY, button);
    }

    /** While open the wheel is modal: it scrolls the popup and is always swallowed so the row list stays put. */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (open == null) return false;
        open.popupMouseScrolled(mouseX, mouseY, scrollY);
        return true;
    }

    public boolean keyPressed(int key, int scan, int mods) {
        return open != null && open.popupKeyPressed(key, scan, mods);
    }

    public boolean charTyped(char c, int mods) {
        return open != null && open.popupCharTyped(c, mods);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (open != null) open.renderPopup(graphics, mouseX, mouseY);
    }
}
