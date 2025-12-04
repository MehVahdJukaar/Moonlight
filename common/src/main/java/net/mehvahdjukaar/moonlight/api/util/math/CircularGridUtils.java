package net.mehvahdjukaar.moonlight.api.util.math;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Streaming ring generator: emits integer lattice points (x,y) for the annulus
 * R <= dist((x,y),(cx,cy)) < R+1.
 * <p>
 * Two APIs:
 * - forEachVec2iInRing(cx, cy, R, IntVec2iConsumer)  // minimal-allocation callback
 * - streamRingVec2is(cx, cy, R) -> Stream<Vec2i>     // stream of Vec2is
 */
public final class CircularGridUtils {

    /**
     * Minimal primitive consumer for (x,y).
     */
    @FunctionalInterface
    public interface IntVec2iConsumer {
        void accept(int x, int y);
    }

    /**
     * Call the consumer for every point in the ring (no allocation of a result set).
     * Very fast; suitable when you want to process points on-the-fly.
     */
    public static void forEachInRing(int cx, int cy, int R, int gridScale, IntVec2iConsumer consumer) {
        if (R < 0) return;
        if (R == 0) {
            consumer.accept(cx, cy);
            return;
        }

        final long Rl = (long) R;
        final long outerBound = (Rl + 1L) * (Rl + 1L) - 1L; // inclusive upper squared-dist
        final long innerBound = Rl * Rl - 1L;              // squared-dist <= innerBound is strictly inside

        // Optional tiny sqrt to reduce dy range; can be avoided by using R + 1
        int maxDy = (int) Math.floor(Math.sqrt(outerBound)); // single small sqrt (safe)
        // If you truly want zero sqrt calls, use: int maxDy = R + 1;

        // monotone-decrement xOuter / xInner across dy so total decrements = O(R)
        int xOuter = R + 1;  // initial upper x estimate (>= actual)
        int xInner = R;      // initial inner x estimate (>= actual or = -1 if innerBound<0)

        for (int dy = 0; dy <= maxDy; dy++) {
            long dy2 = (long) dy * dy;

            // decrease xOuter until it fits
            while (xOuter >= 0 && (long) xOuter * xOuter + dy2 > outerBound) xOuter--;
            // decrease xInner until it fits
            if (innerBound < 0) {
                xInner = -1;
            } else {
                while (xInner >= 0 && (long) xInner * xInner + dy2 > innerBound) xInner--;
            }

            int xLow = xInner + 1;
            int xHigh = xOuter;
            if (xHigh < xLow) {
                // no points in the ring on this scanline
            } else {
                // produce points for y = cy + dy and y = cy - dy (mirror)
                int yPos = cy + dy;
                int yNeg = cy - dy;

                // for each x in [xLow..xHigh], produce (cx + x, y) and (cx - x, y)
                if (dy == 0) {
                    // only one row (no vertical mirroring)
                    for (int x = xLow; x <= xHigh; x++) {
                        consumer.accept(cx + x, yPos);
                        if (x != 0) consumer.accept(cx - x, yPos); // avoid duplicating x==0
                    }
                } else {
                    for (int x = xLow; x <= xHigh; x++) {
                        int xp = cx + x;
                        int xn = cx - x;
                        consumer.accept(xp, yPos);
                        if (x != 0) consumer.accept(xn, yPos);
                        consumer.accept(xp, yNeg);
                        if (x != 0) consumer.accept(xn, yNeg);
                    }
                }
            }
        }
    }

    public static void forEachInRing(int cx, int cy, int R, IntVec2iConsumer consumer) {
        forEachInRing(cx, cy, R, 1, consumer);
    }

    public static Stream<Vec2i> streamRing(int cx, int cy, int R) {
        return streamRing(cx, cy, R, 1);
    }

    /**
     * Returns a Stream<Vec2i> that lazily generates ring points. The stream is
     * sequential (non-parallel) by default. The Vec2is are created on the fly.
     */
    public static Stream<Vec2i> streamRing(int cx, int cy, int R, int gridScale) {
        Spliterator<Vec2i> spl = new RingSpliterator(cx, cy, R, gridScale);
        return StreamSupport.stream(spl, false);
    }

    /**
     * Spliterator that generates Vec2is lazily without precomputing/allocating full set.
     * Characteristics: ORDERED (row-ish), NONNULL, SIZED is not set (unknown size), IMMUTABLE.
     */
    private static final class RingSpliterator extends Spliterators.AbstractSpliterator<Vec2i> {
        // internal ring parameters
        private final int cx, cy, R;
        private final long outerBound, innerBound;
        private final int maxDyInit;
        private final int gridScale;

        // state for generation
        private int dy = 0;         // current dy (0..maxDy)
        private int maxDy;          // computed at construction
        private int xOuter;         // current outer x (monotone non-increasing)
        private int xInner;         // current inner x (monotone non-increasing)
        private int xCurr;          // current x within [xLow..xHigh] for emission
        private int xLow;           // current low bound (xInner+1) for this dy
        private int xHigh;          // current high bound (xOuter) for this dy
        private int rowPhase;       // 0..3 encodes which of the up-to-4 points for a given x are emitted next:

        // mapping we use:
        // rowPhase == 0: preparing a new x (compute bounds), then emit (cx + x, cy + dy)
        // rowPhase == 1: emit (cx - x, cy + dy)  (skip if x==0)
        // rowPhase == 2: emit (cx + x, cy - dy)  (skip if dy==0)
        // rowPhase == 3: emit (cx - x, cy - dy)  (skip if x==0 or dy==0)
        RingSpliterator(int cx, int cy, int R, int gridScale) {
            super(Long.MAX_VALUE, 0); // unknown size; no special characteristics
            this.cx = cx;
            this.cy = cy;
            this.R = R;
            this.gridScale = gridScale;

            if (R < 0) {
                outerBound = -1;
                innerBound = -1;
                maxDyInit = -1;
            } else {
                long Rl = (long) R;
                outerBound = (Rl + 1L) * (Rl + 1L) - 1L;
                innerBound = Rl * Rl - 1L;
                maxDyInit = (int) Math.floor(Math.sqrt(outerBound));
            }

            resetInitialState();
        }

        private void resetInitialState() {
            if (R < 0) {
                maxDy = -1;
                xOuter = 0;
                xInner = -1;
                xCurr = 0;
                xLow = 1;
                xHigh = 0;
                rowPhase = 0;
                return;
            }
            maxDy = maxDyInit;
            xOuter = R + 1;
            xInner = R;
            dy = 0;
            xCurr = 0;
            xLow = 1;
            xHigh = 0; // force computation on first tryAdvance call
            rowPhase = 0;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Vec2i> action) {
            if (R < 0) return false;

            while (true) {
                // if current x range exhausted, advance dy and recompute range
                if (xCurr > xHigh) {
                    if (dy > maxDy) return false; // finished all rows

                    // compute new bounds for this dy
                    long dy2 = (long) dy * dy;
                    while (xOuter >= 0 && (long) xOuter * xOuter + dy2 > outerBound) xOuter--;
                    if (innerBound < 0) {
                        xInner = -1;
                    } else {
                        while (xInner >= 0 && (long) xInner * xInner + dy2 > innerBound) xInner--;
                    }
                    xLow = xInner + 1;
                    xHigh = xOuter;
                    xCurr = xLow;
                    rowPhase = 0;

                    // if nothing on this scanline, advance dy
                    if (xHigh < xLow) {
                        dy++;
                        continue;
                    }
                }

                // emit according to rowPhase for current xCurr
                int x = xCurr;
                int xp = cx + x;
                int xn = cx - x;
                int yPos = cy + dy;
                int yNeg = cy - dy;

                Vec2i p = null;
                switch (rowPhase) {
                    case 0:
                        // (cx + x, yPos)
                        p = new Vec2i(xp, yPos);
                        rowPhase = 1;
                        break;
                    case 1:
                        // (cx - x, yPos) if x != 0
                        if (x != 0) {
                            p = new Vec2i(xn, yPos);
                        }
                        rowPhase = 2;
                        break;
                    case 2:
                        // (cx + x, yNeg) if dy != 0
                        if (dy != 0) {
                            p = new Vec2i(xp, yNeg);
                        }
                        rowPhase = 3;
                        break;
                    case 3:
                        // (cx - x, yNeg) if x != 0 && dy != 0
                        if (x != 0 && dy != 0) {
                            p = new Vec2i(xn, yNeg);
                        }
                        // advance to next x after finishing the 4-phase emission
                        rowPhase = 0;
                        xCurr++;
                        break;
                }

                // If we skipped an emission because of x==0 or dy==0, just loop to emit next available
                if (p == null) {
                    continue;
                }

                action.accept(p);
                return true;
            }
        }
    }


    public static void forEachInDisk(int centerX, int centerY, int radius, IntVec2iConsumer consumer) {
        if (radius <= 0) {
            consumer.accept(centerX, centerY);
            return;
        }

        int rSq = radius * radius;

        for (int dy = -radius; dy <= radius; dy++) {
            int yy = dy * dy;
            int dx = (int) Math.floor(Math.sqrt(rSq - yy)); // one sqrt per row
            int startX = centerX - dx;
            int endX = centerX + dx;
            int py = centerY + dy;
            for (int x = startX; x <= endX; x++) {
                consumer.accept(x, py);
            }
        }

    }
}

