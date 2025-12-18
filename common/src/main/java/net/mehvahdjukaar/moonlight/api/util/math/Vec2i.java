package net.mehvahdjukaar.moonlight.api.util.math;

public record Vec2i(int x, int y) {

    public static final Vec2i ZERO = new Vec2i(0, 0);

    public Vec2i add(Vec2i vec2i) {
        return new Vec2i(this.x + vec2i.x, this.y + vec2i.y);
    }

    public Vec2i subtract(Vec2i vec2i) {
        return new Vec2i(this.x - vec2i.x, this.y - vec2i.y);
    }

    public Vec2i multiply(int scalar) {
        return new Vec2i(this.x * scalar, this.y * scalar);
    }

    public Vec2i scale(int scalar) {
        return new Vec2i(this.x * scalar, this.y * scalar);
    }

    public Vec2i offset(Direction2D direction2D){
        return this.add(direction2D.getStep());
    }
}
