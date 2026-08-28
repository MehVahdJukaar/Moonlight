package net.mehvahdjukaar.moonlight.api.client.gui.widget;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.api.util.TextHelper.formatNumber;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;

public class Vec3ControlWidget extends CompositeWidget {

    @FunctionalInterface
    public interface Sink {
        void accept(double x, double y, double z);
    }

    private static final int INNER_GAP = 3;

    private final EditBox xBox;
    private final EditBox yBox;
    private final EditBox zBox;
    private final List<EditBox> boxes;
    private final double boundLo;
    private final double boundHi;
    private final boolean integer;
    private final Sink onChange;

    public Vec3ControlWidget(int width, int height, double x, double y, double z,
                      double boundLo, double boundHi, boolean integer, Sink onChange) {
        super(0, 0, width, height, Component.empty());
        this.boundLo = boundLo;
        this.boundHi = boundHi;
        this.integer = integer;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        // construct each box at (roughly) its final width so setValue doesn't scroll a short number out of view
        int third = Math.max(1, (width - 2 * INNER_GAP) / 3);
        this.xBox = makeBox(font, third, height, x);
        this.yBox = makeBox(font, third, height, y);
        this.zBox = makeBox(font, third, height, z);
        this.boxes = List.of(xBox, yBox, zBox);
    }

    private EditBox makeBox(Font font, int width, int height, double value) {
        EditBox box = new EditBox(font, 0, 0, width, height, Component.empty());
        box.setMaxLength(Short.MAX_VALUE);
        box.setValue(format(value));
        box.setResponder(s -> onEdited());
        return box;
    }

    public void setValues(double x, double y, double z) {
        this.xBox.setValue(format(x));
        this.yBox.setValue(format(y));
        this.zBox.setValue(format(z));
    }

    private void onEdited() {
        Double px = parse(xBox);
        Double py = parse(yBox);
        Double pz = parse(zBox);
        this.xBox.setTextColor(px != null ? TEXT : ERROR);
        this.yBox.setTextColor(py != null ? TEXT : ERROR);
        this.zBox.setTextColor(pz != null ? TEXT : ERROR);
        if (px != null && py != null && pz != null) {
            this.onChange.accept(px, py, pz);
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int third = (getWidth() - 2 * INNER_GAP) / 3;
        int y = getY();
        this.xBox.setPosition(getX(), y);
        this.xBox.setWidth(third);
        this.yBox.setPosition(getX() + third + INNER_GAP, y);
        this.yBox.setWidth(third);
        this.zBox.setPosition(getX() + 2 * (third + INNER_GAP), y);
        this.zBox.setWidth(third);
        this.xBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.yBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.zBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return boxes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
