package net.mehvahdjukaar.selene.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.minecraft.util.math.BlockPos;

/**
 * utility class do not instance
 * used to create decorations for decoration types that don't have a marker
 */
public class DummyMapWorldMarker extends MapWorldMarker<CustomDecoration> {

    public DummyMapWorldMarker(CustomDecorationType<CustomDecoration,?> type, int x, int z) {
        super(type);
        setPos(new BlockPos(x,64,z));
    }
    @Override
    protected CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,null);
    }
}
