package net.mehvahdjukaar.moonlight.api.client.model;

public class ModelDataKey<T> {

    public ModelDataKey(Class<T> type){
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
