package net.mehvahdjukaar.moonlight.api.client.model;

public interface IExtraModelDataProvider {

    ExtraModelData getExtraModelData();

    default void beforeModelUpdate(){

    };

    default void requestModelReload(){};
}
