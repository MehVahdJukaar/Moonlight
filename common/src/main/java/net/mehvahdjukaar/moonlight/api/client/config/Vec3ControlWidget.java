package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.CompositeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * The editing control for a {@link net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption.Vec3Value}
 * or {@link net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption.Vec3iValue}: three number fields
 * (x, y, z) on a single row. Each field is validated against the shared bounds and turns red when out of range; a
 * fully valid triple is reported back. The {@code integer} flag switches parsing/formatting between int and double.
 */
class Vec3ControlWidget extends CompositeWidget {

    /** Receives a valid x/y/z triple as doubles (the provider rounds to ints for the Vec3i case). */
    @FunctionalInterface
    interface Sink {
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

    Vec3ControlWidget(int width, int height, double x, double y, double z,
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

    /** Pushes the given triple into the fields (used by the row's reset button). */
    void setValues(double x, double y, double z) {
        this.xBox.setValue(format(x));
        this.yBox.setValue(format(y));
        this.zBox.setValue(format(z));
    }

    private void onEdited() {
        Double px = parse(xBox);
        Double py = parse(yBox);
        Double pz = parse(zBox);
        this.xBox.setTextColor(px != null ? TEXT_COLOR : ERROR_COLOR);
        this.yBox.setTextColor(py != null ? TEXT_COLOR : ERROR_COLOR);
        this.zBox.setTextColor(pz != null ? TEXT_COLOR : ERROR_COLOR);
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
        if (integer) return String.valueOf((long) v);
        return v == Math.rint(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int third = (getWidth() - 2 * INNER_GAP) / 3;
        int y = getY();
        this.xBox.setPosition(getX(), y);
        this.xBox.setWidth(third);
        this.yBox.setPosition(getX() + third + INNER_GAP, y);
        this.yBox.setWidth(third);
        this.zBox.setPosition(getX() + 2 * (third + INNER_GAP), y);
        this.zBox.setWidth(third);
        this.xBox.render(graphics, mouseX, mouseY, partialTick);
        this.yBox.render(graphics, mouseX, mouseY, partialTick);
        this.zBox.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return boxes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
