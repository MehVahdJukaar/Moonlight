package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.minecraft.core.BlockPos;

/**
 * utility class do not instance
 * used to merge decorations for decoration types that don't have a block marker (for structure decorations for example)
 */
public class GenericMapBlockMarker<T extends CustomMapDecoration> extends MapBlockMarker<T> {

    public GenericMapBlockMarker(MapDecorationType<T,?> type, int x, int z) {
        this(type,new BlockPos(x,64,z));
    }

    public GenericMapBlockMarker(MapDecorationType<T,?> type, BlockPos pos) {
        this(type);
        this.setPos(pos);
    }

    public GenericMapBlockMarker(MapDecorationType<T,?> type) {
        super(type);
    }

    @Override
    protected T doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return (T) new CustomMapDecoration(this.getType(),mapX,mapY,rot,null);
    }
}
