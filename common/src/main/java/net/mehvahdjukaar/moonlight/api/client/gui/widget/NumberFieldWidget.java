package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.DoubleConsumer;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.api.util.TextHelper.formatNumber;

public class NumberFieldWidget extends CompositeWidget {

    private static final ResourceLocation FIELD = ResourceLocation.withDefaultNamespace("widget/text_field");
    private static final ResourceLocation FIELD_FOCUSED = ResourceLocation.withDefaultNamespace("widget/text_field_highlighted");

    private static final String MINUS = "-";
    private static final String PLUS = "+";

    private static final int STEP_W = 12;  // width of the [-] / [+] zone at each edge
    private static final int TEXT_PAD = 3; // matches the vanilla bordered edit box's text inset
    private static final int GLYPH_H = 8;     // what the vanilla bordered edit box centers its text on
    private static final int BORDER = 0xFFA0A0A0;      // the text field sprite's own border grey
    private static final int BORDER_FOCUSED = 0xFFFFFFFF;
    private static final int ARROW_AT_BOUND = 0xFF5A5A5A;
    private static final int SHIFT_MULTIPLIER = 10;

    private final EditBox box;
    private final List<EditBox> children;
    private final double min;
    private final double max;
    private final double step;
    private final boolean integer;

    public NumberFieldWidget(int width, int height, double initial, double min, double max, boolean integer, DoubleConsumer onChange) {
        super(0, 0, width, height, Component.empty());
        this.min = min;
        this.max = max;
        this.integer = integer;
        this.step = integer ? 1 : 0.1;

        Font font = Minecraft.getInstance().font;
        // Built at its final width so a short number doesn't start scrolled out of view. A plain edit box and not the
        // panning one: numbers are short, and its marquee centers text by its own rule, which would fight textY().
        // The height runs from the text down to the bottom edge, so clicking the number focuses it without the box
        // reaching past the frame
        this.box = new EditBox(font, 0, 0, innerWidth(width), height - (height - GLYPH_H) / 2, Component.empty());
        this.box.setBordered(false); // this widget draws the frame, spanning the step zones too
        this.box.setMaxLength(Short.MAX_VALUE);
        this.box.setValue(format(initial));
        this.box.setResponder(s -> {
            Double parsed = parse(s);
            this.box.setTextColor(parsed != null ? TEXT : ERROR);
            if (parsed != null) onChange.accept(parsed);
        });
        this.children = List.of(box);
    }

    private static int innerWidth(int width) {
        return width - 2 * (STEP_W + 1 + TEXT_PAD);
    }

    // drawn width of a glyph. Font#width counts the trailing spacing column, which would bias the centering
    private static int glyphWidth(Font font, String glyph) {
        return font.width(glyph) - 1;
    }

    // top of every glyph in the widget, arrows and number alike, so they sit on one line
    private static int textY(int y, int height) {
        return y + (height - GLYPH_H) / 2;
    }

    // pushes a value into the field, for the row's reset button
    public void setValue(double v) {
        this.box.setValue(format(v));
    }

    private Double parse(String s) {
        try {
            double v = integer ? Long.parseLong(s.trim()) : Double.parseDouble(s.trim());
            return v >= min && v <= max ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String format(double v) {
        if (integer) return String.valueOf(Math.round(v));
        // stepping accumulates binary fraction noise (1.2 + 0.1 = 1.3000000000000003), so settle it at the step's scale
        return formatNumber(Math.round(v * 10000d) / 10000d);
    }

    // the value a step starts from: whatever is typed if valid, else the nearest bound of the range
    private double currentOrNearest() {
        Double parsed = parse(box.getValue());
        if (parsed != null) return parsed;
        return Math.clamp(0, min, max);
    }

    private boolean canStep(int dir) {
        if (!this.active) return false;
        Double parsed = parse(box.getValue());
        if (parsed == null) return true; // stepping is how you get back to a sane value
        return dir > 0 ? parsed < max : parsed > min;
    }

    private void step(int dir) {
        double from = currentOrNearest();
        double next = Math.clamp(from + dir * step * (Screen.hasShiftDown() ? SHIFT_MULTIPLIER : 1), min, max);
        if (next == from && parse(box.getValue()) != null) return;
        this.box.setValue(format(next)); // the responder commits it
        GuiHelper.playClickSound();
    }

    private boolean overStep(double mouseX, double mouseY, int dir) {
        if (mouseY < getY() || mouseY >= getY() + getHeight()) return false;
        int left = dir > 0 ? getX() + getWidth() - STEP_W : getX();
        return mouseX >= left && mouseX < left + STEP_W;
    }

    private int arrowColor(double mouseX, double mouseY, int dir) {
        if (!canStep(dir)) return ARROW_AT_BOUND;
        return overStep(mouseX, mouseY, dir) ? BORDER_FOCUSED : BORDER;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.active) {
            int dir = overStep(mouseX, mouseY, -1) ? -1 : overStep(mouseX, mouseY, 1) ? 1 : 0;
            if (dir != 0) {
                if (canStep(dir)) step(dir);
                return true; // eat the click either way, the arrow is not a hole in the widget
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean focused = this.box.isFocused();
        graphics.blitSprite(focused ? FIELD_FOCUSED : FIELD, x, y, w, h);

        // the two 1px dividers, and with them the three bands: [border|minus|div|number|div|plus|border]
        int leftDivider = x + STEP_W;
        int rightDivider = x + w - STEP_W - 1;
        int line = focused ? BORDER_FOCUSED : BORDER;
        graphics.fill(leftDivider, y + 1, leftDivider + 1, y + h - 1, line);
        graphics.fill(rightDivider, y + 1, rightDivider + 1, y + h - 1, line);

        Font font = Minecraft.getInstance().font;
        int textY = textY(y, h);
        // each arrow is inset from its own outer edge by the same mirrored amount, so an odd leftover pixel lands on
        // the same side of both and they read as a pair. No shadow: these are chrome, not label text
        int minusW = glyphWidth(font, MINUS);
        int plusW = glyphWidth(font, PLUS);
        graphics.drawString(font, MINUS, x + 1 + (STEP_W - 1 - minusW) / 2, textY,
                arrowColor(mouseX, mouseY, -1), false);
        graphics.drawString(font, PLUS, x + w - 1 - (STEP_W - 1 - plusW) / 2 - plusW, textY,
                arrowColor(mouseX, mouseY, 1), false);

        // the number is centered between the dividers, falling back to left aligned once it no longer fits. An
        // unbordered edit box draws its text at its own y, only a bordered one centers, hence textY here
        int fieldStart = leftDivider + 1 + TEXT_PAD;
        int fieldWidth = rightDivider - TEXT_PAD - fieldStart;
        int slack = Math.max(0, fieldWidth - font.width(this.box.getValue()));
        this.box.setPosition(fieldStart + slack / 2, textY);
        this.box.setWidth(fieldWidth - slack / 2);
        this.box.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
