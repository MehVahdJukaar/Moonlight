package net.mehvahdjukaar.selene.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.minecraft.core.BlockPos;

/**
 * utility class do not instance
 * used to create decorations for decoration types that don't have a block marker (for structure decorations for example)
 */
public class DummyMapBlockMarker<T extends CustomDecoration> extends MapBlockMarker<T> {

    public DummyMapBlockMarker(CustomDecorationType<T,?> type, int x, int z) {
        super(type,new BlockPos(x,64,z));
    }

    @Override
    protected T doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return (T) new CustomDecoration(this.getType(),mapX,mapY,rot,null);
    }
}
