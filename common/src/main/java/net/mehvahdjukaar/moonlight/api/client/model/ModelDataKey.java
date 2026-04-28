package net.mehvahdjukaar.moonlight.api.client.model;

public class ModelDataKey<T> {

    public ModelDataKey(Class<T> type){
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
