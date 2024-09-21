package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;

import java.util.function.Consumer;

public interface IHoldingPlayerExtension {

    void moonlight$setCustomMarkersDirty();

    <H extends CustomMapData.DirtyCounter> void moonlight$setCustomDataDirty(
            CustomMapData.Type<?,?> type, Consumer<H> dirtySetter);
}
