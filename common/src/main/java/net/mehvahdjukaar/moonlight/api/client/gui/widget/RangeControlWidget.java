package net.mehvahdjukaar.moonlight.api.client.gui.widget;


import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
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

public class RangeControlWidget extends CompositeWidget {

    private static final int INNER_GAP = 8;
    private static final String SEPARATOR = "<";

    private final EditBox minBox;
    private final EditBox maxBox;
    private final double boundLo;
    private final double boundHi;
    private final Consumer<Range> onChange;
    private final List<EditBox> boxes;

    public RangeControlWidget(int width, int height, Range initial, double boundLo, double boundHi, Consumer<Range> onChange) {
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
        this.minBox.setValue(formatNumber(initial.min()));
        this.maxBox.setValue(formatNumber(initial.max()));
        this.minBox.setResponder(s -> onEdited());
        this.maxBox.setResponder(s -> onEdited());
        this.boxes = List.of(minBox, maxBox);
    }

    public void setRange(Range range) {
        this.minBox.setValue(formatNumber(range.min()));
        this.maxBox.setValue(formatNumber(range.max()));
    }

    private void onEdited() {
        Double parsedMin = parse(minBox);
        Double parsedMax = parse(maxBox);
        this.minBox.setTextColor(parsedMin != null ? TEXT : ERROR);
        this.maxBox.setTextColor(parsedMax != null ? TEXT : ERROR);
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

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int half = (getWidth() - INNER_GAP) / 2;
        this.minBox.setPosition(getX(), getY());
        this.minBox.setWidth(half);
        this.maxBox.setPosition(getX() + half + INNER_GAP, getY());
        this.maxBox.setWidth(half);
        this.minBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.maxBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        // a small "<" between the two fields to read as min-below-max
        Font font = Minecraft.getInstance().font;
        graphics.text(font, SEPARATOR,
                getX() + half + (INNER_GAP - font.width(SEPARATOR)) / 2 + 1,
                getY() + (getHeight() - font.lineHeight) / 2 + 1, LABEL, false);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return boxes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
