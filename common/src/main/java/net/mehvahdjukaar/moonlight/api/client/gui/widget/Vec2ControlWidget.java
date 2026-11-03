package net.mehvahdjukaar.moonlight.api.client.gui.widget;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.api.util.TextHelper.formatNumber;

public class Vec2ControlWidget extends CompositeWidget {

    @FunctionalInterface
    public interface Sink {
        void accept(double x, double y);
    }

    private static final int INNER_GAP = 3;

    private final EditBox xBox;
    private final EditBox yBox;
    private final List<EditBox> boxes;
    private final double boundLo;
    private final double boundHi;
    private final boolean integer;
    private final Sink onChange;

    public Vec2ControlWidget(int width, int height, double x, double y,
                      double boundLo, double boundHi, boolean integer, Sink onChange) {
        super(0, 0, width, height, Component.empty());
        this.boundLo = boundLo;
        this.boundHi = boundHi;
        this.integer = integer;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        int half = Math.max(1, (width - INNER_GAP) / 2);
        this.xBox = makeBox(font, half, height, x);
        this.yBox = makeBox(font, half, height, y);
        this.boxes = List.of(xBox, yBox);
    }

    private EditBox makeBox(Font font, int width, int height, double value) {
        EditBox box = new EditBox(font, 0, 0, width, height, Component.empty());
        box.setMaxLength(Short.MAX_VALUE);
        box.setValue(format(value));
        box.setResponder(s -> onEdited());
        return box;
    }

    public void setValues(double x, double y) {
        this.xBox.setValue(format(x));
        this.yBox.setValue(format(y));
    }

    private void onEdited() {
        Double px = parse(xBox);
        Double py = parse(yBox);
        this.xBox.setTextColor(px != null ? FIELD_TEXT : ERROR);
        this.yBox.setTextColor(py != null ? FIELD_TEXT : ERROR);
        if (px != null && py != null) {
            this.onChange.accept(px, py);
        }
    }

    private Double parse(EditBox box) {
        try {
            String t = box.getValue().trim();
            double v = integer ? Integer.parseInt(t) : Double.parseDouble(t);
            if (v < boundLo || v > boundHi) return null;
            return v;
        } catch (Exception e) {
            return null;
        }
    }

    private String format(double v) {
        return integer ? String.valueOf((long) v) : formatNumber(v);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int half = (getWidth() - INNER_GAP) / 2;
        int y = getY();
        this.xBox.setPosition(getX(), y);
        this.xBox.setWidth(half);
        this.yBox.setPosition(getX() + half + INNER_GAP, y);
        this.yBox.setWidth(half);
        this.xBox.render(graphics, mouseX, mouseY, partialTick);
        this.yBox.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return boxes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
