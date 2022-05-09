package net.mehvahdjukaar.selene.math;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public record Vec2i(int x, int y) implements Comparable<Vec2i> {

    public Vec2i subtract(Vec2i vec2i) {
        return this.subtract(vec2i.x, vec2i.y);
    }

    public Vec2i subtract(int x, int y) {
        return this.add(-x, -y);
    }

    public Vec2i add(Vec2i vec2i) {
        return this.add(vec2i.x, vec2i.y);
    }

    public Vec2i add(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    public float lengthSqr(){
        return this.x*this.x + this.y*this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Vec2i vec2i) {
            return vec2i.x == this.x && vec2i.y == this.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    @Override
    public int compareTo(@NotNull Vec2i other) {
        return Float.compare(this.lengthSqr(), other.lengthSqr());
    }

}
