package net.mehvahdjukaar.moonlight.api.misc;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

//just a WeakHashMap with dummy values
public class WeakHashSet<T> extends AbstractSet<T> {

    private final Map<T, Object> map;

    public WeakHashSet() {
        map = new WeakHashMap<>();
    }

    // other methods

    @Override
    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public boolean add(T obj) {
        return map.put(obj, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object obj) {
        return map.remove(obj) != null;
    }

    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }
}