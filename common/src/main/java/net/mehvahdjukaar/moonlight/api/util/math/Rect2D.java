package net.mehvahdjukaar.moonlight.api.util.math;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.phys.Vec2;

import java.util.*;

// Simple rectangle representation
public record Rect2D(int x, int y, int width, int height) {

    public static final Codec<Rect2D> CODEC = Codec.INT.listOf().comapFlatMap(
            list -> {
                if (list.size() != 4) return DataResult.error(() -> "Expected list of size 4 for Rect2D");
                return DataResult.success(new Rect2D(list.get(0), list.get(1), list.get(2), list.get(3)));
            },
            rect -> List.of(rect.x, rect.y, rect.width, rect.height)
    );

    public Rect2D(int x, int y, int width, int height) {
        //validate width and height. if negative normalize
        this.x = width < 0 ? x + width + 1 : x;
        this.y = height < 0 ? y + height + 1 : y;
        this.width = Math.abs(width);
        this.height = Math.abs(height);
    }

    public static Rect2D including(Vec2i from, Vec2i to) {
        return new Rect2D(
                Math.min(from.x(), to.x()),
                Math.min(from.y(), to.y()),
                Math.abs(to.x() - from.x()) + 1,
                Math.abs(to.y() - from.y()) + 1
        );
    }

    public Collection<Vec2i> subtract(Rect2D other) {
        List<Vec2i> points = new ArrayList<>();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                Vec2i point = new Vec2i(i, j);
                if (!other.contains(point)) {
                    points.add(point);
                }
            }
        }
        return points;
    }

    public Vec2i topLeft() {
        return new Vec2i(x, y + height - 1);
    }

    public Vec2i topRight() {
        return new Vec2i(x + width - 1, y + height - 1);
    }

    public Vec2i bottomLeft() {
        return new Vec2i(x, y);
    }

    public Vec2i bottomRight() {
        return new Vec2i(x + width - 1, y);
    }

    public int left() {
        return x;
    }

    public int right() {
        return x + width - 1;
    }

    public int bottom() {
        return y;
    }

    public int top() {
        return y + height - 1;
    }

    public Vec2 getCenter() {
        return new Vec2(x + width / 2.0f, y + height / 2.0f);
    }

    public Collection<Vec2i> toPoints() {
        //turn to points
        List<Vec2i> set = new ArrayList<>();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                set.add(new Vec2i(i, j));
            }
        }
        return set;
    }

    public int getArea() {
        return width * height;
    }

    public Iterator<Vec2i> iterateEdge(Direction2D dir) {
        return new Iterator<>() {
            private int index = 0;
            private final int length;
            private final int startX;
            private final int startY;
            private final boolean horizontal;

            {
                switch (dir) {
                    case UP -> {
                        horizontal = true;
                        startX = left();
                        startY = top();
                        length = width;
                    }
                    case DOWN -> {
                        horizontal = true;
                        startX = left();
                        startY = bottom();
                        length = width;
                    }
                    case RIGHT -> {
                        horizontal = false;
                        startX = right();
                        startY = bottom();
                        length = height;
                    }
                    case LEFT -> {
                        horizontal = false;
                        startX = left();
                        startY = bottom();
                        length = height;
                    }
                    default -> throw new IllegalStateException();
                }
            }

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public Vec2i next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                Vec2i p = horizontal
                        ? new Vec2i(startX + index, startY)
                        : new Vec2i(startX, startY + index);

                index++;
                return p;
            }
        };
    }

    public Rect2D containing(Rect2D other) {
        int newX = Math.min(this.x, other.x);
        int newY = Math.min(this.y, other.y);
        int newWidth = Math.max(this.x + this.width, other.x + other.width) - newX;
        int newHeight = Math.max(this.y + this.height, other.y + other.height) - newY;
        return new Rect2D(newX, newY, newWidth, newHeight);
    }

    public Rect2D moveBy(int dx, int dy) {
        return new Rect2D(x + dx, y + dy, width, height);
    }

    public Rect2D moveBy(Vec2i delta) {
        return new Rect2D(x + delta.x(), y + delta.y(), width, height);
    }

    public Rect2D intersect(Rect2D other) {
        int newX = Math.max(this.x, other.x);
        int newY = Math.max(this.y, other.y);
        int newWidth = Math.min(this.x + this.width, other.x + other.width) - newX;
        int newHeight = Math.min(this.y + this.height, other.y + other.height) - newY;
        if (newWidth <= 0 || newHeight <= 0) {
            return new Rect2D(0, 0, 0, 0); // No intersection
        }
        return new Rect2D(newX, newY, newWidth, newHeight);
    }

    public Rect2D expandToward(Direction2D dir) {
        return expandToward(dir.getStep());
    }

    public Rect2D expandToward(Vec2i dir) {
        int newX = dir.x() < 0 ? x + dir.x() : x;
        int newY = dir.y() < 0 ? y + dir.y() : y;
        int newWidth = width + Math.abs(dir.x());
        int newHeight = height + Math.abs(dir.y());
        return new Rect2D(newX, newY, newWidth, newHeight);
    }

    public boolean contains(Vec2i point) {
        return point.x() >= x && point.x() < x + width &&
                point.y() >= y && point.y() < y + height;
    }

    public boolean contains(Rect2D other) {
        if (other == null) return true;
        return this.x <= other.x &&
                this.y <= other.y &&
                this.x + this.width >= other.x + other.width &&
                this.y + this.height >= other.y + other.height;
    }

    public Iterator<Vec2i> iteratePoints() {
        return new Iterator<>() {
            private int currentX = x;
            private int currentY = y;

            @Override
            public boolean hasNext() {
                return currentY < y + height;
            }

            @Override
            public Vec2i next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Vec2i point = new Vec2i(currentX, currentY);
                currentX++;
                if (currentX >= x + width) {
                    currentX = x;
                    currentY++;
                }
                return point;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isSquare() {
        return width == height;
    }
}
