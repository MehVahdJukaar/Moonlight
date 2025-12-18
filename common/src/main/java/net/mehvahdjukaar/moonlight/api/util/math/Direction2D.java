package net.mehvahdjukaar.moonlight.api.util.math;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec2;

import java.util.Locale;

public enum Direction2D implements StringRepresentable {
    UP(0, 1),
    DOWN(0, -1),
    RIGHT(1, 0),
    LEFT(-1, 0);

    public static final Codec<Direction2D> CODEC = StringRepresentable.fromEnum(Direction2D::values);

    final Vec2i step;

    Direction2D(int dx, int dy) {
        this.step = new Vec2i(dx, dy);
    }

    public Vec2i getStep() {
        return step;
    }

    public Direction2D getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public Direction2D clockwise() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    public Direction2D counterClockwise() {
        return switch (this) {
            case UP -> LEFT;
            case LEFT -> DOWN;
            case DOWN -> RIGHT;
            case RIGHT -> UP;
        };
    }

    public static Direction2D closest(Vec2 vec) {
        double absX = Math.abs(vec.x);
        double absY = Math.abs(vec.y);
        if (absX > absY) {
            return vec.x > 0 ? RIGHT : LEFT;
        } else {
            return vec.y > 0 ? UP : DOWN;
        }
    }

    public static Direction2D closest(Vec2i vec) {
        double absX = Math.abs(vec.x());
        double absY = Math.abs(vec.y());
        if (absX > absY) {
            return vec.x() > 0 ? RIGHT : LEFT;
        } else {
            return vec.y() > 0 ? UP : DOWN;
        }
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static Direction2D from3D(Direction dir, Rotation facingRot) {
        dir = facingRot.rotate(dir);
        return switch (dir) {
            case UP -> UP;
            case DOWN -> DOWN;
            case WEST -> LEFT;
            case EAST -> RIGHT;
            default -> throw new IllegalArgumentException("Invalid horizontal direction: " + dir);
        };
    }
}
