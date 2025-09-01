package net.mehvahdjukaar.moonlight.core.pack;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.List;

public final class ImageMerger {

    public enum Mode {
        /**
         * Minimizes canvas area; never upscales. All inputs are downscaled to the smallest side.
         */
        MIN_AREA_NO_UPSCALE,
        /**
         * Never upscales. Uses a uniform tile (max side). Larger images downscale; smaller ones are centered.
         */
        NO_UPSCALE_CENTER
    }

    /**
     * Merge square NativeImages into a single square atlas.
     * Caller owns the lifetime of input images. Returned image is a new instance.
     */
    public static NativeImage mergeSquare(List<NativeImage> images, Mode mode, int backgroundColor) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("images is empty");
        }

        // Validate & find min/max sizes
        int minSide = Integer.MAX_VALUE;
        int maxSide = Integer.MIN_VALUE;
        for (NativeImage img : images) {
            if (img == null) throw new IllegalArgumentException("null image");
            int w = img.getWidth();
            int h = img.getHeight();
            if (w != h) throw new IllegalArgumentException("all images must be square, got " + w + "x" + h);
            minSide = Math.min(minSide, w);
            maxSide = Math.max(maxSide, w);
        }

        final int n = images.size();
        final int g = (int) Math.ceil(Math.sqrt(n)); // grid dim
        final int tile = (mode == Mode.MIN_AREA_NO_UPSCALE) ? minSide : maxSide;
        final int canvas = g * tile;

        NativeImage out = new NativeImage(NativeImage.Format.RGBA, canvas, canvas, true);
        fillColor(out, backgroundColor);

        // how many rows actually used (may be < g)
        final int rowsUsed = (n + g - 1) / g;
        // global vertical pad to center rows when there are fewer than g rows
        final int globalYPadding = ((g - rowsUsed) * tile) / 2;

        for (int i = 0; i < n; i++) {
            NativeImage src = images.get(i);
            int srcSize = src.getWidth(); // square

            // row/col in logical packing (row-major)
            int row = i / g;
            int col = i % g;

            // number of columns in this row (last row may be partial)
            int colsThisRow = (row < rowsUsed - 1) ? g : (n - (rowsUsed - 1) * g);
            if (colsThisRow == 0) colsThisRow = g; // full grid fallback

            // horizontal pad for this row to center its images
            int rowXPadding = ((g - colsThisRow) * tile) / 2;

            // top-left of this cell (now row/col are centered in canvas)
            int cellX = rowXPadding + col * tile;
            int cellY = globalYPadding + row * tile;

            if (mode == Mode.MIN_AREA_NO_UPSCALE) {
                // scale everything to tile == smallest side
                if (srcSize == tile) {
                    blit(src, out, 0, 0, srcSize, cellX, cellY);
                } else {
                    blitScaledNearest(src, out, cellX, cellY, tile);
                }
            } else { // NO_UPSCALE_CENTER
                if (srcSize > tile) {
                    blitScaledNearest(src, out, cellX, cellY, tile);
                } else {
                    int innerPad = (tile - srcSize) / 2; // center inside the cell
                    blit(src, out, 0, 0, srcSize, cellX + innerPad, cellY + innerPad);
                }
            }
        }


        return out;
    }

    /**
     * Fill the whole image with a solid RGBA color (e.g., 0x00000000 for transparent).
     */
    private static void fillColor(NativeImage img, int rgba) {
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setPixelRGBA(x, y, rgba);
            }
        }
    }

    /**
     * Blit a square region (0..size) from src to dst at (dx,dy) 1:1.
     */
    private static void blit(NativeImage src, NativeImage dst, int sx, int sy, int size, int dx, int dy) {
        for (int y = 0; y < size; y++) {
            int srcY = sy + y;
            int dstY = dy + y;
            for (int x = 0; x < size; x++) {
                int rgba = src.getPixelRGBA(sx + x, srcY);
                dst.setPixelRGBA(dx + x, dstY, rgba);
            }
        }
    }

    /**
     * Scale src (square) into a dst square of size dSize at (dx,dy) using nearest neighbor.
     */
    private static void blitScaledNearest(NativeImage src, NativeImage dst, int dx, int dy, int dSize) {
        int s = src.getWidth(); // == src.getHeight()
        // Nearest-neighbor: center-of-pixel sampling
        for (int y = 0; y < dSize; y++) {
            // map [0, dSize) -> [0, s)
            int sy = (int) ((y + 0.5) * s / dSize);
            if (sy >= s) sy = s - 1;
            int dstY = dy + y;

            for (int x = 0; x < dSize; x++) {
                int sx = (int) ((x + 0.5) * s / dSize);
                if (sx >= s) sx = s - 1;

                int rgba = src.getPixelRGBA(sx, sy);
                dst.setPixelRGBA(dx + x, dstY, rgba);
            }
        }
    }
}
