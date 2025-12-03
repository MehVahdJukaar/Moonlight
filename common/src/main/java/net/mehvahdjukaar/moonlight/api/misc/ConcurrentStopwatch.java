package net.mehvahdjukaar.moonlight.api.misc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * A stopwatch that can be started and stopped multiple times concurrently from different threads.
 * It keeps track of the actual in-game time regardless of overlapping intervals.
 */
public final class ConcurrentStopwatch {
    private final LongAdder accumulated = new LongAdder();
    private final AtomicInteger active = new AtomicInteger();
    private final AtomicLong startedAt = new AtomicLong();

    public Timing measure() { return new Timing(this); }

    public void clear() {
        accumulated.reset();
        active.set(0);
        startedAt.set(0);
    }

    public long elapsedNanos() {
        long base = accumulated.sum();
        return active.get() > 0 ? base + (System.nanoTime() - startedAt.get()) : base;
    }

    public double elapsedMillis() { return elapsedNanos() / 1_000_000.0; }

    public double elapsedSeconds() { return elapsedNanos() / 1_000_000_000.0; }

    @Override public String toString() {
        return String.format("Elapsed: %.3f ms (%.6f s)", elapsedMillis(), elapsedSeconds());
    }

    private void onStart() {
        if (active.getAndIncrement() == 0) {
            startedAt.set(System.nanoTime());
        }
    }

    private void onStop() {
        int left = active.decrementAndGet();
        if (left == 0) {
            accumulated.add(System.nanoTime() - startedAt.get());
        } else if (left < 0) {
            active.incrementAndGet();
            throw new IllegalStateException("close() called more times than started");
        }
    }

    public static final class Timing implements AutoCloseable {
        private final ConcurrentStopwatch owner;
        private boolean closed;

        private Timing(ConcurrentStopwatch owner) {
            this.owner = owner;
            owner.onStart();
        }

        @Override public void close() {
            if (!closed) {
                owner.onStop();
                closed = true;
            }
        }
    }
}
