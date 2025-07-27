package net.mehvahdjukaar.moonlight.api.misc;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RollingBuffer<T> implements Iterable<T> {
    private final Object[] buffer;
    private int start = 0;  // oldest element index
    private int size = 0;

    public RollingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        buffer = new Object[capacity];
    }

    public void push(T element) {
        if (size < buffer.length) {
            buffer[(start + size) % buffer.length] = element;
            size++;
        } else {
            buffer[start] = element;
            start = (start + 1) % buffer.length;
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", size: " + size);
        }
        int realIndex = (start + index) % buffer.length;
        return (T) buffer[realIndex];
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return buffer.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == buffer.length;
    }

    public void fillAll(T element) {
        for (int i = 0; i < buffer.length; i++) {
           push(element);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < size;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return get(current++);
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RollingBuffer{");
        for (int i = 0; i < size; i++) {
            sb.append(get(i));
            if (i < size - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
