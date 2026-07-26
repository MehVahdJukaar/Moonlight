package net.mehvahdjukaar.moonlight.api.client.gui.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ScreenParticle {

    private final @Nullable ResourceLocation sprite;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float gravity;
    private float drag;              // fraction of the speed shed per second
    private float rotation;
    private float spin;              // degrees per second
    private float startSize = 4;
    private float endSize = 4;
    private float startAlpha = 1;
    private float endAlpha = 0;
    private int tint = 0xFFFFFF;
    private float lifetime = 1;
    private float age;

    protected ScreenParticle(@Nullable ResourceLocation sprite, float x, float y) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
    }

    /** A particle drawing the given GUI sprite, stretched to its current size. */
    public static ScreenParticle sprite(ResourceLocation sprite, float x, float y) {
        return new ScreenParticle(sprite, x, y);
    }

    /** A particle drawing a plain square. Cheap, and needs no assets. */
    public static ScreenParticle square(float x, float y) {
        return new ScreenParticle(null, x, y);
    }

    public ScreenParticle velocity(float x, float y) {
        this.velocityX = x;
        this.velocityY = y;
        return this;
    }

    /** Downward acceleration in px/s². Negative floats the particle up. */
    public ScreenParticle gravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    /** Air resistance, as the fraction of the current speed lost per second. 0 keeps the particle coasting. */
    public ScreenParticle drag(float drag) {
        this.drag = drag;
        return this;
    }

    public ScreenParticle rotation(float degrees) {
        this.rotation = degrees;
        return this;
    }

    public ScreenParticle spin(float degreesPerSecond) {
        this.spin = degreesPerSecond;
        return this;
    }

    /** Side length in px, eased from birth to death. Pass the same value twice to keep it constant. */
    public ScreenParticle size(float start, float end) {
        this.startSize = start;
        this.endSize = end;
        return this;
    }

    public ScreenParticle size(float size) {
        return size(size, size);
    }

    public ScreenParticle alpha(float start, float end) {
        this.startAlpha = start;
        this.endAlpha = end;
        return this;
    }

    public ScreenParticle tint(int rgb) {
        this.tint = rgb & 0xFFFFFF;
        return this;
    }

    public ScreenParticle lifetime(float seconds) {
        this.lifetime = Math.max(0.01f, seconds);
        return this;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    /** @return false once the particle has outlived its lifetime and should be dropped. */
    public boolean tick(float dt) {
        this.age += dt;
        if (this.age >= this.lifetime) return false;
        this.velocityY += this.gravity * dt;
        if (this.drag > 0) {
            float kept = Math.max(0, 1 - this.drag * dt);
            this.velocityX *= kept;
            this.velocityY *= kept;
        }
        this.x += this.velocityX * dt;
        this.y += this.velocityY * dt;
        this.rotation += this.spin * dt;
        return true;
    }

    public void render(GuiGraphics graphics) {
        float t = this.age / this.lifetime;
        float size = Mth.lerp(t, this.startSize, this.endSize);
        if (size <= 0) return;
        float alpha = Mth.clamp(Mth.lerp(t, this.startAlpha, this.endAlpha), 0, 1);
        if (alpha <= 0) return;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.x, this.y, 0);
        if (this.rotation != 0) pose.mulPose(Axis.ZP.rotationDegrees(this.rotation));
        // the quad is authored as a unit square centered on the origin, so size and rotation are pure transforms and
        // nothing has to be rounded to whole pixels
        pose.scale(size, size, 1);
        pose.translate(-0.5f, -0.5f, 0);
        if (this.sprite == null) {
            graphics.fill(0, 0, 1, 1, FastColor.ARGB32.color(Mth.floor(alpha * 255), this.tint));
        } else {
            graphics.setColor(FastColor.ARGB32.red(this.tint) / 255f, FastColor.ARGB32.green(this.tint) / 255f,
                    FastColor.ARGB32.blue(this.tint) / 255f, alpha);
            graphics.blitSprite(this.sprite, 0, 0, 1, 1);
            graphics.setColor(1, 1, 1, 1);
        }
        pose.popPose();
    }
}
