package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * An {@link EditBox} that, when it isn't focused and its text overflows, marquees the full value back and forth
 * (via {@link GuiHelper#renderScrollingText}) instead of clipping it — the same panning the picker's display value
 * and the option-row labels use. While focused it falls back to the normal vanilla behaviour (cursor-driven
 * horizontal scroll), so editing is unchanged.
 */
public class PanningEditBox extends EditBox {

    private static final ResourceLocation TEXT_FIELD_SPRITE = ResourceLocation.withDefaultNamespace("widget/text_field");

    private final Font font;
    private int textColor = ConfigGuiColors.TEXT;

    public PanningEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.font = font;
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        this.textColor = color; // captured so the idle marquee matches (e.g. red on invalid input)
    }

    private boolean overflows() {
        return this.font.width(this.getValue()) > this.getInnerWidth();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isFocused() || !this.isVisible() || !overflows()) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (this.isBordered()) {
            graphics.blitSprite(TEXT_FIELD_SPRITE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        int textX = this.isBordered() ? this.getX() + 4 : this.getX();
        GuiHelper.renderScrollingText(graphics, this.font, Component.literal(this.getValue()),
                textX, textX + this.getInnerWidth(), this.getY(), this.getHeight(), this.textColor);
    }
}
