package net.mehvahdjukaar.moonlight.api.client.model;

public class ModelDataKey<T> {

    private final Class<T> type;

    public ModelDataKey(Class<T> type){
       this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
