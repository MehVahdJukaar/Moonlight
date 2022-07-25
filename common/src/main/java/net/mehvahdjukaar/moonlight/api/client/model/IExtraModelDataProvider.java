package net.mehvahdjukaar.moonlight.api.client.model;

/**
 * Implement in your tile entity
 */
public interface IExtraModelDataProvider {

    ExtraModelData getExtraModelData();

    default void beforeModelUpdate(){

    };

    default void requestModelReload(){};
}
