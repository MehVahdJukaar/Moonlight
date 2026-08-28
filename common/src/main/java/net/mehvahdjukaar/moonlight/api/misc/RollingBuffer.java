package net.mehvahdjukaar.moonlight.api.misc;

import java.util.AbstractList;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class RollingBuffer<T> extends AbstractList<T> implements RandomAccess {
    private final Object[] buffer;
    private int start = 0; // index of the oldest element
    private int size = 0;

    public RollingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.buffer = new Object[capacity];
    }

    private int cap() { return buffer.length; }

    private int toPhysical(int logicalIndex) {
        // logicalIndex in [0, size)
        return (start + logicalIndex) % cap();
    }

    @SuppressWarnings("unchecked")
    private T elementAtPhysical(int physical) {
        return (T) buffer[physical];
    }

    private void setPhysical(int physical, T e) {
        buffer[physical] = e;
    }

    /** Alias for addLast(e). */
    public void push(T element) {
        addLast(element);
    }

    /** Capacity of the ring. */
    public int capacity() {
        return cap();
    }

    public boolean isFull() {
        return size == cap();
    }

    /** Fill the whole buffer by pushing the same element repeatedly (keeps overwrite-on-full semantics). */
    public void fillAll(T element) {
        for (int i = 0; i < cap(); i++) {
            addLast(element);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        return elementAtPhysical(toPhysical(index));
    }

    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        int p = toPhysical(index);
        @SuppressWarnings("unchecked")
        T old = (T) buffer[p];
        buffer[p] = element;
        return old;
    }

    /** Appends at the tail; if full, overwrites the head (drops oldest), preserving size. */
    @Override
    public boolean add(T element) {
        addLast(element);
        return true;
    }

    /**
     * Indexed adds are only supported at the ends:
     * - index == 0 -> addFirst
     * - index == size() -> addLast
     * Middle inserts are not supported.
     */
    @Override
    public void add(int index, T element) {
        if (index == 0) {
            addFirst(element);
        } else if (index == size) {
            addLast(element);
        } else {
            throw new UnsupportedOperationException("Middle insert not supported in RollingBuffer");
        }
    }

    /** Removes element at index and returns it. Supports removal anywhere (shifts the smaller side). */
    @Override
    public T remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);

        modCount++;

        // Physical position of the removed element
        int removePhys = toPhysical(index);
        @SuppressWarnings("unchecked")
        T removed = (T) buffer[removePhys];

        if (index < size / 2) {
            // Shift [0..index-1] one step towards tail
            for (int i = index; i > 0; i--) {
                int from = toPhysical(i - 1);
                int to = toPhysical(i);
                buffer[to] = buffer[from];
            }
            // Clear new head slot and move start +1
            buffer[start] = null;
            start = (start + 1) % cap();
        } else {
            // Shift [index+1..size-1] one step towards head
            for (int i = index; i < size - 1; i++) {
                int from = toPhysical(i + 1);
                int to = toPhysical(i);
                buffer[to] = buffer[from];
            }
            // Clear old tail slot
            int tailPhys = toPhysical(size - 1);
            buffer[tailPhys] = null;
        }

        size--;
        return removed;
    }

    @Override
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++) {
            buffer[toPhysical(i)] = null;
        }
        start = 0;
        size = 0;
    }

    /** Adds at the logical head; if full, drops the last (most recent) element. */
    @Override
    public void addFirst(T e) {
        modCount++;
        if (size < cap()) {
            start = (start - 1 + cap()) % cap();
            setPhysical(start, e);
            size++;
        } else {
            // Overwrite new head and drop old tail
            start = (start - 1 + cap()) % cap();
            setPhysical(start, e);
            // size unchanged
        }
    }

    /** Adds at the logical tail; if full, drops the head (oldest) element. */
    @Override
    public void addLast(T e) {
        modCount++;
        if (size < cap()) {
            int tailPhys = toPhysical(size);
            setPhysical(tailPhys, e);
            size++;
        } else {
            // Overwrite head, then advance start
            setPhysical(start, e);
            start = (start + 1) % cap();
            // size unchanged
        }
    }

    @Override
    public T getFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return elementAtPhysical(start);
    }

    @Override
    public T getLast() {
        if (isEmpty()) throw new NoSuchElementException();
        int tailPhys = toPhysical(size - 1);
        return elementAtPhysical(tailPhys);
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        modCount++;
        @SuppressWarnings("unchecked")
        T v = (T) buffer[start];
        buffer[start] = null;
        start = (start + 1) % cap();
        size--;
        return v;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) throw new NoSuchElementException();
        modCount++;
        int tailPhys = toPhysical(size - 1);
        @SuppressWarnings("unchecked")
        T v = (T) buffer[tailPhys];
        buffer[tailPhys] = null;
        size--;
        return v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RollingBuffer{");
        for (int i = 0; i < size; i++) {
            sb.append(get(i));
            if (i < size - 1) sb.append(", ");
        }
        sb.append('}');
        return sb.toString();
    }
}
