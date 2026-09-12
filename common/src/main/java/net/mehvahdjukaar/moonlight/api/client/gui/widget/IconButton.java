package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class IconButton extends Button {

    private static final int PAD = 4;

    private final Identifier sprite;
    private Identifier offSprite;
    private final int spriteWidth;
    private final int spriteHeight;
    private boolean drawBackground = true;

    public IconButton(int x, int y, int width, int height, Component message, Identifier sprite, OnPress onPress) {
        this(x, y, width, height, message, sprite, 12, 12, onPress);
    }

    public IconButton(int x, int y, int width, int height, Component message, Identifier sprite,
                      int spriteWidth, int spriteHeight, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.sprite = sprite;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }

    /** Swaps in a different sprite while the button is disabled, instead of dimming the normal one. */
    public IconButton offIcon(Identifier sprite) {
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
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int iconY = this.getY() + (this.getHeight() - this.spriteHeight) / 2;
        int iconX;
        if (drawBackground) {
            this.extractDefaultSprite(graphics);
            this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            if (hasText()) {
                Font font = Minecraft.getInstance().font;
                int textLeft = this.getX() + (this.getWidth() - font.width(this.getMessage())) / 2;
                iconX = Math.max(this.getX() + PAD, textLeft - PAD - this.spriteWidth);
            } else {
                iconX = this.getX() + (this.getWidth() - this.spriteWidth) / 2;
            }
        } else {
            if (this.isHoveredOrFocused()) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ConfigGuiColors.HOVER_HIGHLIGHT);
            }
            iconX = this.getX() + (this.getWidth() - this.spriteWidth) / 2;
        }
        boolean useOffSprite = !this.active && this.offSprite != null;
        int tint = this.active || useOffSprite ? CommonColors.WHITE : CommonColors.GRAY;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, useOffSprite ? this.offSprite : this.sprite,
                iconX, iconY, this.spriteWidth, this.spriteHeight, tint);
    }
}
