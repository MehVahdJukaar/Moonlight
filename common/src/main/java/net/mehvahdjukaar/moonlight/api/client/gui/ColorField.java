package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A color input control: a hex field ({@code #AARRGGBB}) plus a {@link ColorSwatchWidget}. Editing the hex reports
 * the new ARGB color through {@code onChange}; if an {@code onSwatchClick} action is given the swatch becomes a
 * button (typically opening a {@link ColorPickerScreen}), otherwise it is a passive preview. Colors are ARGB ints.
 */
public class ColorField extends CompositeWidget {

    private static final int GAP = 4;

    private final EditBox hexBox;
    private final ColorSwatchWidget swatch;
    private int color;
    private final List<AbstractWidget> children;

    public ColorField(int width, int height, int initial, Consumer<Integer> onChange, @Nullable Consumer<Integer> onSwatchClick) {
        super(0, 0, width, height, Component.empty());
        this.color = initial;

        Font font = Minecraft.getInstance().font;
        int swatchSize = height;
        this.hexBox = new EditBox(font, 0, 0, width - swatchSize - GAP, height, Component.empty());
        this.hexBox.setMaxLength(9);
        this.hexBox.setValue(ColorUtils.toHexString(initial));
        this.swatch = new ColorSwatchWidget(swatchSize, height, initial, onSwatchClick);
        this.hexBox.setResponder(str -> {
            try {
                int c = ColorUtils.parseHex(str);
                this.color = c;
                this.swatch.setColor(c);
                this.hexBox.setTextColor(ConfigGuiColors.TEXT);
                onChange.accept(c);
            } catch (Exception e) {
                this.hexBox.setTextColor(ConfigGuiColors.ERROR);
            }
        });
        this.children = List.of(hexBox, swatch);
    }

    /** Pushes a color into the field and swatch (e.g. from an external reset). */
    public void setColor(int c) {
        this.color = c;
        this.hexBox.setValue(ColorUtils.toHexString(c));
        this.swatch.setColor(c);
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hexBox.setPosition(getX(), getY());
        this.hexBox.render(graphics, mouseX, mouseY, partialTick);
        this.swatch.setPosition(getX() + getWidth() - swatch.getWidth(), getY());
        this.swatch.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
