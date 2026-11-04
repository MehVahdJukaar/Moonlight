package net.mehvahdjukaar.moonlight.api.client.gui.widget;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.api.util.TextHelper.formatNumber;

public class VecControlWidget extends CompositeWidget {

    private static final int INNER_GAP = 3;

    private final EditBox[] boxes;
    private final double boundLo;
    private final double boundHi;
    private final boolean integer;
    private final Consumer<double[]> onChange;

    public VecControlWidget(int width, int height, double[] values,
                            double boundLo, double boundHi, boolean integer, Consumer<double[]> onChange) {
        super(0, 0, width, height, Component.empty());
        this.boundLo = boundLo;
        this.boundHi = boundHi;
        this.integer = integer;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        int boxWidth = boxWidth(width, values.length);
        this.boxes = new EditBox[values.length];
        for (int i = 0; i < values.length; i++) {
            this.boxes[i] = makeBox(font, boxWidth, height, values[i]);
        }
    }

    private static int boxWidth(int width, int count) {
        return Math.max(1, (width - (count - 1) * INNER_GAP) / count);
    }

    private EditBox makeBox(Font font, int width, int height, double value) {
        EditBox box = new EditBox(font, 0, 0, width, height, Component.empty());
        box.setMaxLength(Short.MAX_VALUE);
        box.setValue(format(value));
        box.setResponder(s -> onEdited());
        return box;
    }

    public void setValues(double... values) {
        for (int i = 0; i < boxes.length; i++) {
            this.boxes[i].setValue(format(values[i]));
        }
    }

    private void onEdited() {
        double[] parsed = new double[boxes.length];
        boolean allValid = true;

        for (int i = 0; i < boxes.length; i++) {
            Double v = parse(boxes[i]);
            boxes[i].setTextColor(v != null ? FIELD_TEXT : ERROR);
            if (v == null) allValid = false;
            else parsed[i] = v;
        }
        if (allValid) {
            this.onChange.accept(parsed);
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
        int boxWidth = boxWidth(getWidth(), boxes.length);
        int x = getX();
        for (EditBox box : boxes) {
            box.setPosition(x, getY());
            box.setWidth(boxWidth);
            box.extractRenderState(graphics, mouseX, mouseY, partialTick);
            x += boxWidth + INNER_GAP;
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(boxes);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
