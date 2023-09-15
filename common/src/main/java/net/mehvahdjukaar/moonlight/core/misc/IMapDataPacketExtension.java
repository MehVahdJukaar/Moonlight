package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;

import java.util.Collection;

public interface IMapDataPacketExtension {

    void moonlight$sendCustomDecorations(Collection<CustomMapDecoration> decorations);

    void moonlight$sendCustomMapData(Collection<CustomMapData> data);

    void moonlight$sendMapCenter(int centerX, int centerZ);
}
