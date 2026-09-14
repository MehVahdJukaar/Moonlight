package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** Color preview square. Acts as a button when onPress is not null. */
public class ColorSwatchWidget extends AbstractWidget {

    private int color;
    @Nullable
    private final Consumer<Integer> onPress;

    public ColorSwatchWidget(int width, int height, int color, @Nullable Consumer<Integer> onPress) {
        super(0, 0, width, height, Component.empty());
        this.color = color;
        this.onPress = onPress;
        this.active = onPress != null;
        if (onPress != null) {
            this.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.color_pick")));
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiHelper.renderChecker(graphics, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        int border = onPress != null && isHovered() ? CommonColors.WHITE : CommonColors.BLACK;
        graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.onPress != null) this.onPress.accept(this.color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
