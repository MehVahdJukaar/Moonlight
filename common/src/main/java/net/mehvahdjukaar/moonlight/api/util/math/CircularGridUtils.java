package net.mehvahdjukaar.moonlight.api.util.math;

import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;

import java.util.Iterator;
import java.util.NoSuchElementException;
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
        R = Math.floorDiv(R, gridScale);
        if (R == 0) {
            consumer.accept( cx * gridScale, cy * gridScale);
            return;
        }

        final long Rl = (long) R;
        final long outerBound = (Rl + 1L) * (Rl + 1L) - 1L; // inclusive upper squared-dist
        final long innerBound = Rl * Rl - 1L;              // squared-dist <= innerBound is strictly inside

        // Optional tiny sqrt to reduce dy range; can be avoided by using R + 1
        int maxDy = (int) Math.floor(Math.sqrt(outerBound));
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

            if (xHigh >= xLow) {
                int yPos = (cy + dy) * gridScale;
                int yNeg = (cy - dy) * gridScale;

                if (dy == 0) {
                    for (int x = xLow; x <= xHigh; x++) {
                        int xp = (cx + x) * gridScale;
                        int xn = (cx - x) * gridScale;
                        consumer.accept(xp, yPos);
                        if (x != 0) consumer.accept(xn, yPos);
                    }
                } else {
                    for (int x = xLow; x <= xHigh; x++) {
                        int xp = (cx + x) * gridScale;
                        int xn = (cx - x) * gridScale;
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

    /**
     * Iterator implementation for Point objects
     */
    private static class RingIterator implements Iterator<Vec2i> {
        private final int cx, cy, gridScale;
        private final long Rl, outerBound, innerBound;
        private final int maxDy;

        // State variables
        private int dy = 0;
        private int xOuter, xInner;
        private int currentX;
        private int currentXLow, currentXHigh;
        private int pointsGenerated = 0;
        private int pointsInCurrentRow = 0;
        private boolean initialized = false;
        private boolean hasNext = true;

        // For symmetry handling
        private boolean yPositive = true;
        private boolean xPositive = true;

        RingIterator(int cx, int cy, int R, int gridScale) {
            this.cx = cx;
            this.cy = cy;
            this.gridScale = gridScale;
            R = Math.floorDiv(R, gridScale);

            if (R == 0) {
                // Special case: single point
                this.Rl = 0;
                this.outerBound = 0;
                this.innerBound = -1;
                this.maxDy = 0;
                this.xOuter = 0;
                this.xInner = -1;
                this.currentXLow = 0;
                this.currentXHigh = 0;
                this.initialized = true;
                this.pointsInCurrentRow = 1;
                return;
            }

            this.Rl = (long) R;
            this.outerBound = (Rl + 1L) * (Rl + 1L) - 1L;
            this.innerBound = Rl * Rl - 1L;

            // Optional: remove sqrt if desired
            this.maxDy = (int) Math.floor(Math.sqrt(outerBound));
            // Or: this.maxDy = R + 1;

            this.xOuter = R + 1;
            this.xInner = R;
        }

        private void init() {
            if (initialized) return;

            computeXBoundsForCurrentDy();
            if (currentXHigh >= currentXLow) {
                currentX = currentXLow;
                pointsInCurrentRow = computePointsForCurrentX();
            } else {
                advanceDy();
            }
            initialized = true;
        }

        private void computeXBoundsForCurrentDy() {
            long dy2 = (long) dy * dy;

            // Decrease xOuter until it fits
            while (xOuter >= 0 && (long) xOuter * xOuter + dy2 > outerBound) {
                xOuter--;
            }

            // Decrease xInner until it fits
            if (innerBound < 0) {
                xInner = -1;
            } else {
                while (xInner >= 0 && (long) xInner * xInner + dy2 > innerBound) {
                    xInner--;
                }
            }

            currentXLow = xInner + 1;
            currentXHigh = xOuter;
        }

        private int computePointsForCurrentX() {
            if (dy == 0) {
                // For dy=0, we have 1 or 2 points (depending on x)
                return (currentX == 0) ? 1 : 2;
            } else {
                // For dy>0, we have 2 or 4 points (depending on x)
                return (currentX == 0) ? 2 : 4;
            }
        }

        private void advanceX() {
            currentX++;
            if (currentX > currentXHigh) {
                advanceDy();
            } else {
                pointsInCurrentRow = computePointsForCurrentX();
                pointsGenerated = 0;
                yPositive = true;
                xPositive = true;
            }
        }

        private void advanceDy() {
            dy++;
            if (dy > maxDy) {
                hasNext = false;
                return;
            }

            computeXBoundsForCurrentDy();
            if (currentXHigh >= currentXLow) {
                currentX = currentXLow;
                pointsInCurrentRow = computePointsForCurrentX();
                pointsGenerated = 0;
                yPositive = true;
                xPositive = true;
            } else {
                advanceDy(); // Recursive, but max recursion depth = maxDy
            }
        }

        @Override
        public boolean hasNext() {
            if (!initialized) init();
            return hasNext;
        }

        @Override
        public Vec2i next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            int x, y;

            if (Rl == 0) {
                // Special case for R=0
                hasNext = false;
                return new Vec2i(cx * gridScale, cy * gridScale);
            }

            if (dy == 0) {
                // Handle dy=0 case
                if (currentX == 0) {
                    x = cx * gridScale;
                    y = cy * gridScale;
                    advanceX();
                } else {
                    if (xPositive) {
                        x = (cx + currentX) * gridScale;
                        y = cy * gridScale;
                        xPositive = false;
                    } else {
                        x = (cx - currentX) * gridScale;
                        y = cy * gridScale;
                        advanceX();
                    }
                }
            } else {
                // Handle dy>0 case
                if (currentX == 0) {
                    if (yPositive) {
                        x = cx * gridScale;
                        y = (cy + dy) * gridScale;
                        yPositive = false;
                    } else {
                        x = cx * gridScale;
                        y = (cy - dy) * gridScale;
                        advanceX();
                    }
                } else {
                    if (yPositive) {
                        if (xPositive) {
                            x = (cx + currentX) * gridScale;
                            y = (cy + dy) * gridScale;
                            xPositive = false;
                        } else {
                            x = (cx - currentX) * gridScale;
                            y = (cy + dy) * gridScale;
                            yPositive = false;
                            xPositive = true;
                        }
                    } else {
                        if (xPositive) {
                            x = (cx + currentX) * gridScale;
                            y = (cy - dy) * gridScale;
                            xPositive = false;
                        } else {
                            x = (cx - currentX) * gridScale;
                            y = (cy - dy) * gridScale;
                            advanceX();
                        }
                    }
                }
            }

            return new Vec2i(x, y);
        }
    }

    public static Iterator<Vec2i> iterateInRing(int cx, int cy, int R) {
        return new RingIterator(cx, cy, R, 1);
    }

    public static Iterator<Vec2i> iterateInRing(int cx, int cy, int R, int gridScale) {
        return new RingIterator(cx, cy, R, gridScale);
    }


    public static void forEachInDisk(int centerX, int centerY, int radius, IntVec2iConsumer consumer) {
        forEachInDisk(centerX, centerY, radius, 1, consumer);
    }

    public static void forEachInDisk(int centerX, int centerY, int radius, int gridScale, IntVec2iConsumer consumer) {
        if (radius <= 0) {
            consumer.accept(centerX * gridScale, centerY * gridScale);
            return;
        }

        int rSq = radius * radius;

        for (int dy = -radius; dy <= radius; dy++) {
            int yy = dy * dy;
            int dx = (int) Math.floor(Math.sqrt(rSq - yy)); // one sqrt per row
            int startX = (centerX - dx) * gridScale;
            int endX = (centerX + dx) * gridScale;
            int py = (centerY + dy) * gridScale;
            for (int x = startX; x <= endX; x += gridScale) { // step by gridScale
                consumer.accept(x, py);
            }
        }

    }

}

