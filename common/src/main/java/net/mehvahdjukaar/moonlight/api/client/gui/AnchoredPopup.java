package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper.isMouseOver;

/** A popup hanging off a widget, like a dropdown list or the pickers under a date field. Any click outside closes it. */
public abstract class AnchoredPopup implements Popup {

    protected static final int MARGIN = 2;

    protected final AbstractWidget anchor;
    protected int width;
    protected int height;
    @Nullable
    private OverlayLayer layer;

    protected AnchoredPopup(AbstractWidget anchor, int width, int height) {
        this.anchor = anchor;
        this.width = width;
        this.height = height;
    }

    public void open() {
        if (Minecraft.getInstance().screen instanceof PopupHost host) {
            this.layer = host.getOverlayLayer();
            onOpened();
            this.layer.open(this);
        }
    }

    public void close() {
        if (layer != null) layer.close(this);
    }

    public boolean isOpen() {
        return layer != null;
    }

    protected void onOpened() {
    }

    @Override
    public void onPopupClosed() {
        this.layer = null;
    }

    protected int x() {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return Mth.clamp(anchor.getX() + anchor.getWidth() - width, MARGIN, screenW - width - MARGIN);
    }

    protected int y() {
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int below = anchor.getY() + anchor.getHeight();
        if (below + height + MARGIN <= screenH) return below;
        return Math.max(MARGIN, anchor.getY() - height);
    }

    protected abstract void renderContent(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY);

    protected abstract void clickContent(double mouseX, double mouseY, int x, int y);

    protected void clickOutside(MouseButtonEvent event, boolean doubleClick) {
        close();
    }

    @Override
    public void renderPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = x(), y = y();
        graphics.nextStratum();
        graphics.fill(x, y, x + width, y + height, ConfigGuiColors.POPUP_BG);
        renderContent(graphics, x, y, mouseX, mouseY);
        graphics.outline(x, y, width, height, CommonColors.WHITE);
    }

    @Override
    public boolean popupMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = x(), y = y();
        if (isMouseOver(event.x(), event.y(), x, y, width, height)) {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) clickContent(event.x(), event.y(), x, y);
        } else {
            clickOutside(event, doubleClick);
        }
        return true;
    }

    @Override
    public boolean popupKeyPressed(KeyEvent event) {
        if (event.key() != InputConstants.KEY_ESCAPE) return false;
        close();
        return true;
    }
}
