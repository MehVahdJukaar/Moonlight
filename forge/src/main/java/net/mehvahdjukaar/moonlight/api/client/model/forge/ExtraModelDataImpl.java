package net.mehvahdjukaar.moonlight.api.client.model.forge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ExtraModelDataImpl implements ExtraModelData {

    private static final BiMap<ModelDataKey<?>, ModelProperty<?>> KEYS = HashBiMap.create();
    private final IModelData data;
    private final Set<ModelProperty<?>> properties = new HashSet<>();

    public ExtraModelDataImpl(IModelData data) {
        this.data = data;
    }

    public ExtraModelDataImpl(IModelData data, Set<ModelProperty<?>> p) {
        this.data = data;
        this.properties.addAll(p);
    }

    //TODO: check
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraModelDataImpl that = (ExtraModelDataImpl) o;
        if (!Objects.equals(properties, that.properties)) return false;
        for (var p : properties) {
            if (!Objects.equals(data.getData(p), that.data.getData(p))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, properties);
    }

    @Nullable
    @Override
    public <T> T getData(ModelDataKey<T> key) {
        ModelProperty<T> prop = (ModelProperty<T>) KEYS.get(key);
        if (prop == null) return null;
        return data.getData(prop);
    }

    public static ExtraModelData.Builder builder() {
        return new Builder();
    }

    public IModelData getData() {
        return data;
    }

    public static class Builder implements ExtraModelData.Builder {

        private final ModelDataMap.Builder map = new ModelDataMap.Builder();
        private final Set<ModelProperty<?>> properties = new HashSet<>();

        Builder() {
        }

        @Override
        public <A> ExtraModelData.Builder withProperty(ModelDataKey<A> key, A data) {
            ModelProperty<A> prop = (ModelProperty<A>) KEYS.computeIfAbsent(key, this::makeProp);
            map.withInitial(prop, data);
            properties.add(prop);
            return this;
        }

        private <A> ModelProperty<A> makeProp(ModelDataKey<A> key) {
            return new ModelProperty<>();
        }

        @Override
        public ExtraModelDataImpl build() {
            return new ExtraModelDataImpl(map.build(), properties);
        }
    }


}
