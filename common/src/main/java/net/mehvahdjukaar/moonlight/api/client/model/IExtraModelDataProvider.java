package net.mehvahdjukaar.moonlight.api.client.model;

import java.util.Objects;

/**
 * Implement in your tile entity
 */
public interface IExtraModelDataProvider {

    ExtraModelData getExtraModelData();

    default void requestModelReload() {
    }

    default void afterDataPacket(ExtraModelData oldData) {
        if (!Objects.equals(oldData, this.getExtraModelData())) {
            //this request render data refresh
            this.requestModelReload();
        }
    }
}
