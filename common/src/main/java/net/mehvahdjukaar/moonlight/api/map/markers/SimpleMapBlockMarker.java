package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;

/**
 * used to add decorations for decoration types that don't have a block marker (for structure decorations for example)
 * also used for json defined ones
 */
public class SimpleMapBlockMarker extends MapBlockMarker<CustomMapDecoration> {

    public SimpleMapBlockMarker(MapDecorationType<CustomMapDecoration, ?> type) {
        super(type);
    }

    @Override
    protected CustomMapDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomMapDecoration(this.getType(), mapX, mapY, rot, getName());
    }
}
