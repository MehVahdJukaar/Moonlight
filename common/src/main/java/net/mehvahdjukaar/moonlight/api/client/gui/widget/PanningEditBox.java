package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
