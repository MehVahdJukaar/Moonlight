package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;

public interface Sampler2D {

    int sample(float x, float y);

    static Sampler2D nearest(Sampler2D base) {
        return (x, y) -> base.sample(Math.round(x), Math.round(y));
    }

    static Sampler2D bilinear(Sampler2D base) {
        return (float x, float y) -> {
            int x0 = Mth.floor(x);
            int y0 = Mth.floor(y);
            float dx = x - x0;
            float dy = y - y0;

            if (dx == 0 && dy == 0) {
                return base.sample(x0, y0);
            }

            int c00 = base.sample(x0, y0);
            int c10 = base.sample(x0 + 1, y0);
            int c01 = base.sample(x0, y0 + 1);
            int c11 = base.sample(x0 + 1, y0 + 1);

            int top = ColorUtils.lerp(c00, c10, dx);
            int bottom = ColorUtils.lerp(c01, c11, dx);

            return ColorUtils.lerp(top, bottom, dy);
        };
    }

    /**
     * Averages the base over a footprintW x footprintH area centered on the sampled point, weighting every
     * pixel by how much of it the area covers. Use this when shrinking by more than 2x: nearest and bilinear
     * only ever look at one or four source pixels and skip everything in between.
     * Colors are weighted by alpha so transparent pixels don't bleed their color into the result.
     */
    static Sampler2D box(Sampler2D base, float footprintW, float footprintH) {
        return (x, y) -> {
            float left = x - footprintW / 2 + 0.5f;
            float right = left + footprintW;
            float top = y - footprintH / 2 + 0.5f;
            float bottom = top + footprintH;

            float alphaSum = 0, red = 0, green = 0, blue = 0, covered = 0;
            for (int py = Mth.floor(top); py < Mth.ceil(bottom); py++) {
                float rowWeight = Math.min(bottom, py + 1) - Math.max(top, py);
                for (int px = Mth.floor(left); px < Mth.ceil(right); px++) {
                    float weight = rowWeight * (Math.min(right, px + 1) - Math.max(left, px));
                    int color = base.sample(px, py);
                    float alphaWeight = ARGB.alpha(color) * weight;
                    alphaSum += alphaWeight;
                    red += ARGB.blue(color) * alphaWeight;
                    green += ARGB.green(color) * alphaWeight;
                    blue += ARGB.red(color) * alphaWeight;
                    covered += weight;
                }
            }
            if (covered == 0) return base.sample(x, y);
            if (alphaSum == 0) return 0;
            return ARGB.color(Math.round(alphaSum / covered),
                    Math.round(blue / alphaSum), Math.round(green / alphaSum), Math.round(red / alphaSum));
        };
    }

    static Sampler2D paletted(Sampler2D base, Palette palette) {
        return (x, y) -> {
            int color = base.sample(x, y);
            return palette.getColorClosestTo(new PaletteColor(color)).value();
        };
    }

    static Sampler2D offset(Sampler2D base, float ox, float oy) {
        return (x, y) -> base.sample(x + ox, y + oy);
    }

    static Sampler2D scale(Sampler2D base, float sx, float sy) {
        return (x, y) -> base.sample(x * sx, y * sy);
    }

    /**
     * Width and height are those of the base. A 90 degree rotation is sampled with swapped dimensions.
     */
    static Sampler2D rotate(Sampler2D base, Rotation rotation, int width, int height) {
        return switch (rotation) {
            case NONE -> base;
            case CLOCKWISE_90 -> (x, y) -> base.sample(y, height - 1 - x);
            case CLOCKWISE_180 -> (x, y) -> base.sample(width - 1 - x, height - 1 - y);
            case COUNTERCLOCKWISE_90 -> (x, y) -> base.sample(width - 1 - y, x);
        };
    }

    static Sampler2D flippedX(Sampler2D base, int width) {
        return (x, y) -> base.sample(width - 1 - x, y);
    }

    static Sampler2D flippedY(Sampler2D base, int height) {
        return (x, y) -> base.sample(x, height - 1 - y);
    }

    /**
     * Rounds to the nearest pixel and keeps it inside the image.
     */
    static Sampler2D clamp(Sampler2D base, int width, int height) {
        return (x, y) -> base.sample(Mth.clamp(Math.round(x), 0, width - 1), Mth.clamp(Math.round(y), 0, height - 1));
    }

    static Sampler2D wrap(Sampler2D base, int width, int height) {
        return (x, y) -> {
            int ix = Math.floorMod(Math.round(x), width);
            int iy = Math.floorMod(Math.round(y), height);
            return base.sample(ix, iy);
        };
    }
}
