package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class ExtraModelDataImpl implements ExtraModelData {

    private final Map<ModelDataKey<?>, Object> backingMap;

    public ExtraModelDataImpl(Map<ModelDataKey<?>, Object> map) {
        this.backingMap = new IdentityHashMap<>(map);
    }

    public <T> T getData(ModelDataKey<T> prop) {
        return (T) this.backingMap.get(prop);
    }

    public static ExtraModelData.Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraModelDataImpl that = (ExtraModelDataImpl) o;
        return Objects.equals(backingMap, that.backingMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backingMap);
    }

    public static class Builder implements ExtraModelData.Builder {

        private final Map<ModelDataKey<?>, Object> map;

        Builder() {
            this.map = new HashMap<>();
        }

        @Override
        public <A> ExtraModelData.Builder withProperty(ModelDataKey<A> prop, A data) {
            map.put(prop, data);
            return this;
        }

        @Override
        public ExtraModelData build() {
            return new ExtraModelDataImpl(map);
        }
    }
}
