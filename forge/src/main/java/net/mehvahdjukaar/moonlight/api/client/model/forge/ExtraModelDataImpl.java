package net.mehvahdjukaar.moonlight.api.client.model.forge;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ExtraModelDataImpl(ModelData data) implements ExtraModelData {

    private static final Object2ObjectArrayMap<ModelDataKey<?>, ModelProperty<?>> KEYS_TO_PROP = new Object2ObjectArrayMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraModelDataImpl that = (ExtraModelDataImpl) o;
        if (!Objects.equals(this.data.getProperties(), that.data.getProperties())) return false;
        for (var p : this.data.getProperties()) {
            if (!Objects.equals(data.get(p), that.data.get(p))) return false;
        }
        return true;
    }

    @Nullable
    @Override
    public <T> T get(ModelDataKey<T> key) {
        ModelProperty<T> prop = (ModelProperty<T>) KEYS_TO_PROP.get(key);
        if (prop == null) return null;
        return data.get(prop);
    }

    public static ExtraModelData.Builder builder() {
        return new Builder();
    }

    private static class Builder implements ExtraModelData.Builder {

        private final ModelData.Builder map = ModelData.builder();

        Builder() {
        }

        @Override
        public <A> ExtraModelData.Builder with(ModelDataKey<A> key, A data) {
            ModelProperty<A> prop = (ModelProperty<A>) KEYS_TO_PROP.computeIfAbsent(key, k -> new ModelProperty<>());
            map.with(prop, data);
            return this;
        }

        @Override
        public ExtraModelDataImpl build() {
            return new ExtraModelDataImpl(map.build());
        }
    }


}
