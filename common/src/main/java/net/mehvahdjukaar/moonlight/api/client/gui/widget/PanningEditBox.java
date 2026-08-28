package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PanningEditBox extends EditBox {

    private static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("widget/text_field");

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
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isFocused() || !this.isVisible() || !overflows()) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (this.isBordered()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXT_FIELD_SPRITE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        int textX = this.isBordered() ? this.getX() + 4 : this.getX();
        GuiHelper.renderScrollingText(graphics, this.font, Component.literal(this.getValue()),
                textX, textX + this.getInnerWidth(), this.getY(), this.getHeight(), this.textColor);
    }
}
