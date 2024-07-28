package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.map.type.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MlMapDecorationType;

/**
 * used to add decorations for decoration types that don't have a block marker (for structure decorations for example)
 * also used for json defined ones
 */
public class SimpleMapBlockMarker extends MapBlockMarker<MLMapDecoration> {

    public SimpleMapBlockMarker(MlMapDecorationType<MLMapDecoration, ?> type) {
        super(type);
    }

    @Override
    protected MLMapDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new MLMapDecoration(this.getType(), mapX, mapY, rot, getName());
    }
}
