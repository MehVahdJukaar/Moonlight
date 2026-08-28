package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.ColorPickerScreen;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Hex color field plus a swatch. Colors are ARGB ints, or plain RGB when hasAlpha is false. */
public class ColorFieldWidget extends CompositeWidget {

    private static final int GAP = 4;

    private final EditBox hexBox;
    private final ColorSwatchWidget swatch;
    private final boolean hasAlpha;
    private int color;
    private final List<AbstractWidget> children;

    public ColorFieldWidget(int width, int height, int initial, Consumer<Integer> onChange, @Nullable Consumer<Integer> onSwatchClick) {
        this(width, height, initial, true, onChange, onSwatchClick);
    }

    public ColorFieldWidget(int width, int height, int initial, boolean hasAlpha,
                            Consumer<Integer> onChange, @Nullable Consumer<Integer> onSwatchClick) {
        super(0, 0, width, height, Component.empty());
        this.hasAlpha = hasAlpha;
        this.color = sanitize(initial);

        Font font = Minecraft.getInstance().font;
        int swatchSize = height;
        this.hexBox = new EditBox(font, 0, 0, width - swatchSize - GAP, height, Component.empty());
        this.hexBox.setMaxLength(hasAlpha ? 9 : 7);
        this.hexBox.setValue(ColorUtils.toHexString(color, hasAlpha));
        this.swatch = new ColorSwatchWidget(swatchSize, height, opaqueIfNeeded(color), onSwatchClick);
        this.hexBox.setResponder(str -> {
            try {
                int c = sanitize(ColorUtils.parseHex(str));
                this.color = c;
                this.swatch.setColor(opaqueIfNeeded(c));
                this.hexBox.setTextColor(ConfigGuiColors.TEXT);
                onChange.accept(c);
            } catch (Exception e) {
                this.hexBox.setTextColor(ConfigGuiColors.ERROR);
            }
        });
        this.children = List.of(hexBox, swatch);
    }

    private int sanitize(int c) {
        return hasAlpha ? c : c & 0xFFFFFF;
    }

    // an alpha-less color has zeroed alpha bits, so the preview has to force it opaque to show anything
    private int opaqueIfNeeded(int c) {
        return hasAlpha ? c : c | 0xFF000000;
    }

    public void setColor(int c) {
        this.color = sanitize(c);
        this.hexBox.setValue(ColorUtils.toHexString(color, hasAlpha));
        this.swatch.setColor(opaqueIfNeeded(color));
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hexBox.setPosition(getX(), getY());
        this.hexBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.swatch.setPosition(getX() + getWidth() - swatch.getWidth(), getY());
        this.swatch.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
