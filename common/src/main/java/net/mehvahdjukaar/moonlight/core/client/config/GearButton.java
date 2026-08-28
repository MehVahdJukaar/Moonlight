package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

public class GearButton extends Button {

    private static final int SPRITE_SIZE = 16;
    private static final float SECONDS_PER_TURN = 32f;
    private static final float HOVER_SCALE = 1.25f;
    private static final float SCALE_APPROACH = 10f; // fraction of the remaining distance covered per second

    private float scale = 1f;
    private long lastMs = -1;

    public GearButton(int x, int y, int size, OnPress onPress) {
        super(x, y, size, size, Component.empty(), onPress, DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.mods_button")));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float dt = lastMs < 0 ? 0 : Math.min((now - lastMs) / 1000f, 0.1f); // clamp big gaps (e.g. screen reopen)
        lastMs = now;

        float target = this.isHoveredOrFocused() ? HOVER_SCALE : 1f;
        scale += (target - scale) * Math.min(1f, SCALE_APPROACH * dt);
        float angle = (now % (long) (SECONDS_PER_TURN * 1000)) / (SECONDS_PER_TURN * 1000) * 360f;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(this.getX() + this.getWidth() / 2f, this.getY() + this.getHeight() / 2f);
        pose.rotate(angle * Mth.DEG_TO_RAD);
        pose.scale(scale, scale);
        pose.translate(-SPRITE_SIZE / 2f, -SPRITE_SIZE / 2f);
        int tint = this.active ? 0xFFFFFFFF : 0xFF808080;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MoonlightIcons.CONFIG, 0, 0, SPRITE_SIZE, SPRITE_SIZE, tint);
        pose.popMatrix();
    }
}