package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends Button {

    private static final int PAD = 4;

    private final ResourceLocation sprite;
    private ResourceLocation offSprite;
    private final int spriteWidth;
    private final int spriteHeight;
    private boolean drawBackground = true;

    public IconButton(int x, int y, int width, int height, Component message, ResourceLocation sprite, OnPress onPress) {
        this(x, y, width, height, message, sprite, 12, 12, onPress);
    }

    public IconButton(int x, int y, int width, int height, Component message, ResourceLocation sprite,
                      int spriteWidth, int spriteHeight, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.sprite = sprite;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }

    /** Swaps in a different sprite while the button is disabled, instead of dimming the normal one. */
    public IconButton offIcon(ResourceLocation sprite) {
        this.offSprite = sprite;
        return this;
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
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (hasText()) {
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
        boolean off = !this.active && this.offSprite != null;
        boolean dim = !this.active && !off;
        if (dim) graphics.setColor(0.5f, 0.5f, 0.5f, 1f);
        graphics.blitSprite(off ? this.offSprite : this.sprite, iconX, iconY, this.spriteWidth, this.spriteHeight);
        if (dim) graphics.setColor(1f, 1f, 1f, 1f);
    }
}
