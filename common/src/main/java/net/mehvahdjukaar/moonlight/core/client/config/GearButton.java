package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

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
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float dt = lastMs < 0 ? 0 : Math.min((now - lastMs) / 1000f, 0.1f); // clamp big gaps (e.g. screen reopen)
        lastMs = now;

        float target = this.isHoveredOrFocused() ? HOVER_SCALE : 1f;
        scale += (target - scale) * Math.min(1f, SCALE_APPROACH * dt);
        float angle = (now % (long) (SECONDS_PER_TURN * 1000)) / (SECONDS_PER_TURN * 1000) * 360f;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.getX() + this.getWidth() / 2f, this.getY() + this.getHeight() / 2f, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        pose.scale(scale, scale, 1);
        pose.translate(-SPRITE_SIZE / 2f, -SPRITE_SIZE / 2f, 0);
        if (!this.active) graphics.setColor(0.5f, 0.5f, 0.5f, 1f);
        graphics.blitSprite(ConfigScreenLayout.CONFIG_ICON, 0, 0, SPRITE_SIZE, SPRITE_SIZE);
        if (!this.active) graphics.setColor(1f, 1f, 1f, 1f);
        pose.popPose();
    }
}