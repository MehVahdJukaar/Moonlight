package net.mehvahdjukaar.moonlight.api.util.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record Range(double min, double max) {

    public static final Codec<Range> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("min").forGetter(Range::min),
            Codec.DOUBLE.fieldOf("max").forGetter(Range::max)
    ).apply(i, Range::new));

    public static Range of(double min, double max) {
        return new Range(min, max);
    }

    public double clamp(double value) {
        return Mth.clamp(value, min, max);
    }

    public int clampInt(double value) {
        return (int) Math.round(clamp(value));
    }

    public boolean contains(double value) {
        return value >= min && value <= max;
    }

    public boolean contains(Range other) {
        return other.min >= min && other.max <= max;
    }

    public boolean intersects(Range other) {
        return min <= other.max && other.min <= max;
    }

    public double size() {
        return max - min;
    }

    public double mid() {
        return (min + max) * 0.5;
    }

    public double lerp(double t) {
        return Mth.lerp(t, min, max);
    }

    public double inverseLerp(double value) {
        double s = size();
        return s == 0 ? 0 : (value - min) / s;
    }

    public double random(RandomSource random) {
        return min + random.nextDouble() * size();
    }

    public int randomInt(RandomSource random) {
        return (int) Math.round(random(random));
    }

    public Range normalized() {
        return min <= max ? this : new Range(max, min);
    }

    public boolean isValid() {
        return min <= max;
    }

    public Range withMin(double newMin) {
        return new Range(newMin, max);
    }

    public Range withMax(double newMax) {
        return new Range(min, newMax);
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}
