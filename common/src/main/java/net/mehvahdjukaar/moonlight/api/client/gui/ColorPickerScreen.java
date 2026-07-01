package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * A standalone color picker screen: a saturation/value square with a hue slider beside it and an alpha slider
 * below, plus a hex field and live preview underneath (a {@link ColorField} whose swatch is a passive preview).
 * On Done it hands the chosen ARGB color back through {@code onApply} and returns to {@code parent}; on Cancel it
 * just returns to {@code parent}. Colors are ARGB ints.
 */
public class ColorPickerScreen extends Screen {

    private static final int GAP = 4;
    private static final int CONTROL_HEIGHT = 20;
    private static final int TOP_MARGIN = 44;
    private static final int TITLE_COLOR = ConfigGuiColors.TITLE;

    private final Screen parent;
    private final Consumer<Integer> onApply;

    private float hue, sat, val, alpha; // all 0..1

    private int svX, svY, svSize;
    private int hueX, hueY, hueW, hueH;
    private int alphaX, alphaY, alphaW, alphaH;

    private ColorField control;
    private boolean suppressControlSync;
    private int dragging = DRAG_NONE;
    private static final int DRAG_NONE = 0, DRAG_SV = 1, DRAG_HUE = 2, DRAG_ALPHA = 3;

    public ColorPickerScreen(int color, Screen parent, Consumer<Integer> onApply) {
        super(Component.translatable("gui.moonlight.config.color_picker"));
        this.parent = parent;
        this.onApply = onApply;
        float[] hsv = ColorUtils.argbToHsv(color);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.alpha = FastColor.ARGB32.alpha(color) / 255f;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        this.svSize = 120;
        this.hueW = 14;
        this.alphaH = 12;

        // one centered block: [SV square | hue bar] with the alpha bar and the hex+preview control stacked under it
        int blockW = svSize + GAP + hueW;
        int blockX = cx - blockW / 2;
        int blockH = svSize + 10 + alphaH + 12 + CONTROL_HEIGHT;
        // center the block on the screen's vertical midpoint, clamped so it never overlaps the header or the buttons
        int buttonsY = this.height - 30;
        int top = Mth.clamp((this.height - blockH) / 2, TOP_MARGIN + 8, buttonsY - blockH - 8);

        this.svX = blockX;
        this.svY = top;
        this.hueX = svX + svSize + GAP;
        this.hueY = svY;
        this.hueH = svSize;
        this.alphaX = svX;
        this.alphaY = svY + svSize + 10;
        this.alphaW = blockW;

        this.control = new ColorField(blockW, CONTROL_HEIGHT, currentColor(), this::onControlColorChanged, null);
        this.control.setPosition(blockX, alphaY + alphaH + 12);
        this.addRenderableWidget(this.control);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            onApply.accept(currentColor());
            onClose();
        }).bounds(cx - 100, this.height - 30, 96, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(cx + 4, this.height - 30, 96, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // ===== value helpers =====

    private int currentColor() {
        return ColorUtils.hsvToArgb(hue, sat, val, Math.round(alpha * 255));
    }

    /** Pushes the current color into the hex+preview control (after a drag). */
    private void syncControl() {
        this.suppressControlSync = true;
        this.control.setColor(currentColor());
        this.suppressControlSync = false;
    }

    /** The control's hex field was edited: adopt that color into our hsv/alpha state. */
    private void onControlColorChanged(int c) {
        if (suppressControlSync) return;
        float[] hsv = ColorUtils.argbToHsv(c);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.alpha = FastColor.ARGB32.alpha(c) / 255f;
    }

    // ===== input =====

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, svX, svY, svSize, svSize)) {
            dragging = DRAG_SV;
            updateDrag(mouseX, mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, hueX, hueY, hueW, hueH)) {
            dragging = DRAG_HUE;
            updateDrag(mouseX, mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, alphaX, alphaY, alphaW, alphaH)) {
            dragging = DRAG_ALPHA;
            updateDrag(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging != DRAG_NONE) {
            updateDrag(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = DRAG_NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateDrag(double mouseX, double mouseY) {
        switch (dragging) {
            case DRAG_SV -> {
                this.sat = Mth.clamp((float) (mouseX - svX) / svSize, 0, 1);
                this.val = Mth.clamp(1 - (float) (mouseY - svY) / svSize, 0, 1);
            }
            case DRAG_HUE -> this.hue = Mth.clamp((float) (mouseY - hueY) / hueH, 0, 1);
            case DRAG_ALPHA -> this.alpha = Mth.clamp((float) (mouseX - alphaX) / alphaW, 0, 1);
        }
        syncControl();
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ===== render =====

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, TITLE_COLOR);

        renderSvSquare(graphics);
        renderHueBar(graphics);
        renderAlphaBar(graphics);
    }

    private void renderSvSquare(GuiGraphics graphics) {
        // one vertical gradient per column: full value at top -> black at bottom, saturation increasing rightwards
        for (int i = 0; i < svSize; i++) {
            float s = (float) i / svSize;
            int top = ColorUtils.hsvToArgb(hue, s, 1f, 255);
            graphics.fillGradient(svX + i, svY, svX + i + 1, svY + svSize, top, 0xFF000000);
        }
        graphics.renderOutline(svX - 1, svY - 1, svSize + 2, svSize + 2, 0xFF000000);
        int cxp = svX + Math.round(sat * svSize);
        int cyp = svY + Math.round((1 - val) * svSize);
        ring(graphics, cxp, cyp);
    }

    private void renderHueBar(GuiGraphics graphics) {
        for (int i = 0; i < hueH; i++) {
            graphics.fill(hueX, hueY + i, hueX + hueW, hueY + i + 1, ColorUtils.hsvToArgb((float) i / hueH, 1, 1, 255));
        }
        graphics.renderOutline(hueX - 1, hueY - 1, hueW + 2, hueH + 2, 0xFF000000);
        int y = hueY + Math.round(hue * hueH);
        graphics.fill(hueX - 2, y - 1, hueX + hueW + 2, y + 1, 0xFFFFFFFF);
    }

    private void renderAlphaBar(GuiGraphics graphics) {
        ColorSwatchWidget.renderChecker(graphics, alphaX, alphaY, alphaW, alphaH);
        int rgb = currentColor() & 0x00FFFFFF;
        for (int i = 0; i < alphaW; i++) {
            int a = Math.round((float) i / alphaW * 255);
            graphics.fill(alphaX + i, alphaY, alphaX + i + 1, alphaY + alphaH, (a << 24) | rgb);
        }
        graphics.renderOutline(alphaX - 1, alphaY - 1, alphaW + 2, alphaH + 2, 0xFF000000);
        int x = alphaX + Math.round(alpha * alphaW);
        graphics.fill(x - 1, alphaY - 2, x + 1, alphaY + alphaH + 2, 0xFFFFFFFF);
    }

    private static void ring(GuiGraphics graphics, int cx, int cy) {
        graphics.renderOutline(cx - 3, cy - 3, 6, 6, 0xFFFFFFFF);
        graphics.renderOutline(cx - 4, cy - 4, 8, 8, 0xFF000000);
    }
}
