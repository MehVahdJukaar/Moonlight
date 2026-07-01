package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * The editing control for a {@link net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption.RangeValue}:
 * two number fields (min and max) laid out on a single row. Each field is validated against the shared bounds and
 * turns red when out of range; a valid pair is reported back as a {@link Range}.
 */
class RangeControlWidget extends AbstractContainerWidget {

    private static final int INNER_GAP = 4;

    private final EditBox minBox;
    private final EditBox maxBox;
    private final double boundLo;
    private final double boundHi;
    private final Consumer<Range> onChange;
    private final List<EditBox> boxes;

    RangeControlWidget(int width, int height, Range initial, double boundLo, double boundHi, Consumer<Range> onChange) {
        super(0, 0, width, height, Component.empty());
        this.boundLo = boundLo;
        this.boundHi = boundHi;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        int half = (width - INNER_GAP) / 2;
        this.minBox = new EditBox(font, 0, 0, half, height, Component.empty());
        this.maxBox = new EditBox(font, 0, 0, half, height, Component.empty());
        this.minBox.setMaxLength(Short.MAX_VALUE);
        this.maxBox.setMaxLength(Short.MAX_VALUE);
        this.minBox.setValue(format(initial.min()));
        this.maxBox.setValue(format(initial.max()));
        this.minBox.setResponder(s -> onEdited());
        this.maxBox.setResponder(s -> onEdited());
        this.boxes = List.of(minBox, maxBox);
    }

    /** Pushes the given range into the fields (used by the row's reset button). */
    void setRange(Range range) {
        this.minBox.setValue(format(range.min()));
        this.maxBox.setValue(format(range.max()));
    }

    private void onEdited() {
        Double parsedMin = parse(minBox);
        Double parsedMax = parse(maxBox);
        this.minBox.setTextColor(parsedMin != null ? TEXT_COLOR : ERROR_COLOR);
        this.maxBox.setTextColor(parsedMax != null ? TEXT_COLOR : ERROR_COLOR);
        if (parsedMin != null && parsedMax != null) {
            this.onChange.accept(new Range(parsedMin, parsedMax));
        }
    }

    private Double parse(EditBox box) {
        try {
            double v = Double.parseDouble(box.getValue().trim());
            if (v < boundLo || v > boundHi) return null;
            return v;
        } catch (Exception e) {
            return null;
        }
    }

    private static String format(double v) {
        return v == Math.rint(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int half = (getWidth() - INNER_GAP) / 2;
        this.minBox.setPosition(getX(), getY());
        this.minBox.setWidth(half);
        this.maxBox.setPosition(getX() + half + INNER_GAP, getY());
        this.maxBox.setWidth(half);
        this.minBox.render(graphics, mouseX, mouseY, partialTick);
        this.maxBox.render(graphics, mouseX, mouseY, partialTick);
        // a small dash between the two fields to read as a range
        graphics.drawString(Minecraft.getInstance().font, "-",
                getX() + half + (INNER_GAP - Minecraft.getInstance().font.width("-")) / 2 + 1,
                getY() + (getHeight() - Minecraft.getInstance().font.lineHeight) / 2 + 1, LABEL_COLOR, false);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return boxes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
