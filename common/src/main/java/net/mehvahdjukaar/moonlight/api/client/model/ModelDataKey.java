package net.mehvahdjukaar.moonlight.api.client.model;

import java.util.Objects;

public record ModelDataKey<T>(Class<T> type) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelDataKey<?> that = (ModelDataKey<?>) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
