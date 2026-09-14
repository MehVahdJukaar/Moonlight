package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A button with a sprite drawn to the LEFT of its label. The label works exactly like a vanilla button's, centered
 * and scrolling when too long, and the icon goes just left of it, or in the middle when there is no label. Vanilla's
 * SpriteIconButton only does icon on the right or icon alone, so this covers "[icon] Label".
 */
public class IconButton extends Button {

    private static final int PAD = 4;

    private final ResourceLocation sprite;
    private final int spriteWidth;
    private final int spriteHeight;
    private boolean drawBackground = true;

    public IconButton(int x, int y, int width, int height, Component message, ResourceLocation sprite,
                      int spriteWidth, int spriteHeight, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.sprite = sprite;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }

    /** Drops the button background/label: just the icon, centered, with a faint hover highlight. */
    public IconButton borderless() {
        this.drawBackground = false;
        return this;
    }

    private boolean hasText() {
        return !this.getMessage().getString().isEmpty();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int iconY = this.getY() + (this.getHeight() - this.spriteHeight) / 2;
        int iconX;
        if (drawBackground) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick); // background + vanilla-centered label
            if (hasText()) {
                // vanilla centers the label on the button's midpoint; drop the icon just left of the text's left edge
                Font font = Minecraft.getInstance().font;
                int textLeft = this.getX() + (this.getWidth() - font.width(this.getMessage())) / 2;
                iconX = Math.max(this.getX() + PAD, textLeft - PAD - this.spriteWidth);
            } else {
                iconX = this.getX() + (this.getWidth() - this.spriteWidth) / 2;
            }
        } else {
            if (this.isHoveredOrFocused()) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x30FFFFFF);
            }
            iconX = this.getX() + (this.getWidth() - this.spriteWidth) / 2;
        }
        if (!this.active) graphics.setColor(0.5f, 0.5f, 0.5f, 1f); // dim the icon to match the disabled button
        graphics.blitSprite(this.sprite, iconX, iconY, this.spriteWidth, this.spriteHeight);
        if (!this.active) graphics.setColor(1f, 1f, 1f, 1f);
    }
}
