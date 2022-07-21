package net.mehvahdjukaar.moonlight.api.client.model.forge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ExtraModelDataImpl(ModelData data) implements ExtraModelData {

    private static final BiMap<ModelDataKey<?>, ModelProperty<?>> KEYS = HashBiMap.create();

    //TODO: check
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
        ModelProperty<T> prop = (ModelProperty<T>) KEYS.get(key);
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
            ModelProperty<A> prop = (ModelProperty<A>) KEYS.computeIfAbsent(key, this::makeProp);
            map.with(prop, data);
            return this;
        }

        private <A> ModelProperty<A> makeProp(ModelDataKey<A> key) {
            return new ModelProperty<>();
        }

        @Override
        public ExtraModelDataImpl build() {
            return new ExtraModelDataImpl(map.build());
        }
    }


}
