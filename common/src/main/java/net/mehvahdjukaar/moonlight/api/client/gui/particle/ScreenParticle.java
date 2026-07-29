package net.mehvahdjukaar.moonlight.api.client.gui.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScreenParticle {

    private final List<ResourceLocation> sprites;

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
    private float endAlpha = 1;
    private float fadeOutStart = 1;  // life fraction at which the fade to fully transparent begins
    private int tint = 0xFFFFFF;
    private float lifetime = 1;
    private float age;

    protected ScreenParticle(List<ResourceLocation> sprites, float x, float y) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
    }

    /** A particle drawing the given GUI sprite, stretched to its current size. */
    public static ScreenParticle sprite(ResourceLocation sprite, float x, float y) {
        return new ScreenParticle(List.of(sprite), x, y);
    }

    /** A particle playing the given GUI sprites as an animation, spread evenly over its lifetime. */
    public static ScreenParticle animated(List<ResourceLocation> frames, float x, float y) {
        if (frames.isEmpty()) throw new IllegalArgumentException("Animated screen particle needs at least one frame");
        return new ScreenParticle(List.copyOf(frames), x, y);
    }

    /** A particle drawing one sprite picked at random out of the given ones. */
    public static ScreenParticle randomSprite(List<ResourceLocation> choices, RandomSource random, float x, float y) {
        return sprite(choices.get(random.nextInt(choices.size())), x, y);
    }

    /** A particle drawing a plain square. Cheap, and needs no assets. */
    public static ScreenParticle square(float x, float y) {
        return new ScreenParticle(List.of(), x, y);
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

    /** Deceleration rate: the speed decays exponentially, shedding roughly this fraction of it per second. 0 keeps the particle coasting. */
    public ScreenParticle drag(float drag) {
        this.drag = Math.max(0, drag);
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

    public ScreenParticle alpha(float alpha) {
        return alpha(alpha, alpha);
    }

    public ScreenParticle fadeOut(float lifeFraction) {
        this.fadeOutStart = Mth.clamp(lifeFraction, 0, 1);
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
            // exponential decay so the same drag decelerates the same amount regardless of frame rate
            float kept = (float) Math.exp(-this.drag * dt);
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
        if (t > this.fadeOutStart) {
            alpha *= 1 - Mth.inverseLerp(t, this.fadeOutStart, 1);
        }
        if (alpha <= 0) return;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.x, this.y, 0);
        if (this.rotation != 0) pose.mulPose(Axis.ZP.rotationDegrees(this.rotation));
        // the quad is authored as a unit square centered on the origin, so size and rotation are pure transforms and
        // nothing has to be rounded to whole pixels
        pose.scale(size, size, 1);
        pose.translate(-0.5f, -0.5f, 0);
        if (this.sprites.isEmpty()) {
            graphics.fill(0, 0, 1, 1, FastColor.ARGB32.color(Mth.floor(alpha * 255), this.tint));
        } else {
            graphics.setColor(FastColor.ARGB32.red(this.tint) / 255f, FastColor.ARGB32.green(this.tint) / 255f,
                    FastColor.ARGB32.blue(this.tint) / 255f, alpha);
            graphics.blitSprite(this.currentFrame(t), 0, 0, 1, 1);
            graphics.setColor(1, 1, 1, 1);
        }
        pose.popPose();
    }

    private ResourceLocation currentFrame(float lifeFraction) {
        int frames = this.sprites.size();
        if (frames == 1) return this.sprites.getFirst();
        return this.sprites.get(Mth.clamp(Mth.floor(lifeFraction * frames), 0, frames - 1));
    }
}
