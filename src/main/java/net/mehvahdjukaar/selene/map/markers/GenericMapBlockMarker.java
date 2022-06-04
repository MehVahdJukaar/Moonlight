package net.mehvahdjukaar.selene.map.markers;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.type.IMapDecorationType;
import net.minecraft.core.BlockPos;

/**
 * utility class do not instance
 * used to create decorations for decoration types that don't have a block marker (for structure decorations for example)
 */
public class GenericMapBlockMarker<T extends CustomMapDecoration> extends MapBlockMarker<T> {

    public GenericMapBlockMarker(IMapDecorationType<T,?> type, int x, int z) {
        this(type,new BlockPos(x,64,z));
    }

    public GenericMapBlockMarker(IMapDecorationType<T,?> type, BlockPos pos) {
        this(type);
        this.setPos(pos);
    }

    public GenericMapBlockMarker(IMapDecorationType<T,?> type) {
        super(type);
    }

    @Override
    protected T doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return (T) new CustomMapDecoration(this.getType(),mapX,mapY,rot,null);
    }
}
