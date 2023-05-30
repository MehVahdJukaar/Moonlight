package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.JsonDecorationType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * used to merge decorations for decoration types that don't have a block marker (for structure decorations for example)
 * also used for json defined ones
 */
public final class DummyMapBlockMarker<T extends CustomMapDecoration> extends MapBlockMarker<T> {

    public DummyMapBlockMarker(MapDecorationType<T, ?> type, BlockPos pos) {
        this(type);
        this.setPos(pos);
    }

    public DummyMapBlockMarker(MapDecorationType<T, ?> type) {
        super(type);
    }

    @Override
    public float getRotation() {
        //just forward statically defined rotation
        if (this.getType() instanceof JsonDecorationType t) {
            return t.getRotation();
        }
        return super.getRotation();
    }

    @Override
    protected T doCreateDecoration(byte mapX, byte mapY, byte rot) {
        var v = (T) new CustomMapDecoration(this.getType(), mapX, mapY, rot, null);
        //just forward statically defined name
        if (this.getType() instanceof JsonDecorationType t) {
            t.getName().ifPresent(n -> v.setDisplayName(Component.literal(n)));
        }
        return v;
    }
}
