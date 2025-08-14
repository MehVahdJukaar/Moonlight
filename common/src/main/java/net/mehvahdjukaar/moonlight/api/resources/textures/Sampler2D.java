package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;

public interface Sampler2D {

    int sample(float x, float y);

    static Sampler2D nearest(Sampler2D base) {
        return (x, y) -> base.sample(Math.round(x), Math.round(y));
    }

    static Sampler2D bilinear(Sampler2D base) {
        return (float x, float y) -> {
            int x0 = (int) x;
            int y0 = (int) y;

            if (x0 == x && y0 == y) {
                return base.sample(x0, y0);
            }

            int x1 = x0 + 1;
            int y1 = y0 + 1;

            float dx = x - x0;
            float dy = y - y0;

            int c00 = base.sample(x0, y0);
            int c10 = base.sample(x1, y0);
            int c01 = base.sample(x0, y1);
            int c11 = base.sample(x1, y1);

            int top = ColorUtils.lerp(c00, c10, dx);
            int bottom = ColorUtils.lerp(c01, c11, dx);

            return ColorUtils.lerp(top, bottom, dy);
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

    static Sampler2D rotate(Sampler2D base, Rotation rotation, int width, int height) {
        return switch (rotation) {
            case NONE -> base;
            case CLOCKWISE_90 -> (x, y) -> base.sample(y, width  - x);
            case CLOCKWISE_180 -> (x, y) -> base.sample(width  - x, height - y);
            case COUNTERCLOCKWISE_90 -> (x, y) -> base.sample(height - y, x);
        };
    }

    static Sampler2D flippedX(Sampler2D base, int width) {
        return (x, y) -> base.sample(width - x, y);
    }

    static Sampler2D flippedY(Sampler2D base, int height) {
        return (x, y) -> base.sample(x, height  - y);
    }

    static Sampler2D clamp(Sampler2D base, int width, int height) {
        return (x, y) -> {
            int ix = (int) Mth.clamp(x,0,width);
            int iy = (int) Mth.clamp(y,0,height);
            return base.sample(ix, iy);
        };
    }

    static Sampler2D wrap(Sampler2D base, int width, int height) {
        return (x, y) -> {
            int ix = ((int) Math.floor(x) % width + width) % width;
            int iy = ((int) Math.floor(y) % height + height) % height;
            return base.sample(ix, iy);
        };
    }
}
