package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * represents a block tracker instance which keeps track of a placed block and creates its associated map decoration
 *
 * @param <D> decoration
 */
public abstract class MapBlockMarker<D extends CustomMapDecoration> {
    private final MapDecorationType<D, ?> type;
    private BlockPos pos;
    private int rot =0;

    protected MapBlockMarker(MapDecorationType<D, ?> type) {
        this.type = type;
    }

    protected MapBlockMarker(MapDecorationType<D, ?> type, BlockPos pos) {
        this(type);
        this.pos = pos;
    }

    /**
     * load a world marker to nbt. must match saveToNBT
     * implement if you are adding extra data
     */
    public void loadFromNBT(CompoundTag compound) {
        this.pos = NbtUtils.readBlockPos(compound.getCompound("Pos"));
    }

    /**
     * save a world marker to nbt. must match the factory function provided to the decoration type
     * implement if you are adding extra data
     *
     * @return nbt
     */
    public CompoundTag saveToNBT(CompoundTag compound) {
        compound.put("Pos", NbtUtils.writeBlockPos(this.getPos()));
        return compound;
    }

    /**
     * implement if you are adding extra data
     * see default example for an implementation
     *
     * @param other other marker object
     * @return true if they are equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            MapBlockMarker<?> marker = (MapBlockMarker<?>) other;
            return Objects.equals(this.getPos(), marker.getPos());
        } else {
            return false;
        }
    }

    /**
     * implement if you are adding extra data
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getPos());
    }

    /**
     * ids have to be unique so add here all the data you have in your marker to tell them apart
     *
     * @return suffix
     */
    private String getPosSuffix() {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public MapDecorationType<D, ?> getType() {
        return type;
    }

    public String getTypeId() {
        return Utils.getID(this.type).toString();
    }

    public String getMarkerId() {
        return this.getTypeId() + "-" + getPosSuffix();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public float getRotation() {
        return 0;
    }

    /**
     * creates a decoration given its map position and rotation
     *
     * @param mapX map x position
     * @param mapY map y position
     * @param rot  decoration rotation
     * @return decoration instance
     */
    @Nullable
    protected abstract D doCreateDecoration(byte mapX, byte mapY, byte rot);

    /**
     * creates a decoration from this marker. This its default vanilla implementation.<br>
     * You can do here extra checks for dimension type and so on.<br>
     * For everything else just implement doCreateDecoration
     *
     * @param scale     current map scale
     * @param x         current map origin x
     * @param z         current map origin z
     * @param dimension dimension this map is in
     * @param locked    is this map locked
     * @return new decoration instance
     */
    @Nullable
    public D createDecorationFromMarker(byte scale, int x, int z, ResourceKey<Level> dimension, boolean locked) {
        double worldX = this.getPos().getX();
        double worldZ = this.getPos().getZ();
        double rotation = this.getRotation();

        int i = 1 << scale;
        float f = (float) (worldX - x) / i;
        float f1 = (float) (worldZ - z) / i;
        byte mapX = (byte) ((int) ((f * 2.0F) + 0.5D));
        byte mapY = (byte) ((int) ((f1 * 2.0F) + 0.5D));
        byte rot;
        if (f >= -64.0F && f1 >= -64.0F && f <= 64.0F && f1 <= 64.0F) {
            rotation = rotation + (rotation < 0.0D ? -8.0D : 8.0D);
            rot = (byte) ((int) (rotation * 16.0D / 360.0D));
            return doCreateDecoration(mapX, mapY, rot);
        }
        return null;
    }


    /**
     * used to check if a world marker has changed and needs to update its decoration
     * as an example it can be used when a tile entity changes its name and its decoration needs to reflect that
     *
     * @param other marker that needs to be compared wit this
     * @return true if corresponding decoration has to be updated
     */
    public boolean shouldUpdate(MapBlockMarker<?> other) {
        return false;
    }

    /**
     * updates my map decoration after should update returns true
     */
    public void updateDecoration(CustomMapDecoration old) {
    }

}
