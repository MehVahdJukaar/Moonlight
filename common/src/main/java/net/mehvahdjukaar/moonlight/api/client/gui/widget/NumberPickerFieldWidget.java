package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.AnchoredPopup;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import java.util.function.Function;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

public class NumberPickerFieldWidget extends AbstractWidget {

    @FunctionalInterface
    public interface Sink {
        void accept(int first, int second);
    }

    private final String separator;
    private final Identifier icon;
    private final AnchoredPopup picker;
    private final Sink onChange;
    private int first;
    private int second;

    public NumberPickerFieldWidget(int width, int height, String separator, int first, int second, Identifier icon,
                                   Function<NumberPickerFieldWidget, AnchoredPopup> picker, Sink onChange) {
        super(0, 0, width, height, Component.empty());
        this.separator = separator;
        this.first = first;
        this.second = second;
        this.icon = icon;
        this.picker = picker.apply(this);
        this.onChange = onChange;
    }

    public void setValues(int first, int second) {
        this.first = first;
        this.second = second;
        this.onChange.accept(first, second);
    }

    public int firstValue() {
        return first;
    }

    public int secondValue() {
        return second;
    }

    static String twoDigits(int v) {
        return v < 10 ? "0" + v : String.valueOf(v);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        picker.open();
        GuiHelper.playClickSound();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean open = picker.isOpen();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EditBox.SPRITES.get(true, open), x, y, w, h);

        int iconBox = h;
        int sepX = x + w - iconBox;
        graphics.fill(sepX, y + 1, sepX + 1, y + h - 1, open ? CommonColors.WHITE : CommonColors.LIGHT_GRAY);
        int iconSize = 9;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, sepX + 1 + (iconBox - 1 - iconSize) / 2,
                y + (h - iconSize) / 2, iconSize, iconSize, CommonColors.WHITE);

        Font font = Minecraft.getInstance().font;
        String text = twoDigits(first) + " " + separator + " " + twoDigits(second);
        graphics.centeredText(font, text, x + (w - iconBox) / 2, y + (h - font.lineHeight) / 2 + 1, active ? TEXT : DISABLED);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
