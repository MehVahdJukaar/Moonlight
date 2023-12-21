package net.mehvahdjukaar.moonlight.api.map.markers;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * represents a block tracker instance which keeps track of a placed block and creates its associated map decoration
 *
 * @param <D> decoration
 */
public abstract class MapBlockMarker<D extends CustomMapDecoration> {
    //Static ref is fine as data registry cant change with reload command. Just don't hold ref outside of a world
    protected final MapDecorationType<D, ?> type;
    @Nullable
    private BlockPos pos;
    private int rot = 0;
    @Nullable
    private Component name;
    private boolean persistent;

    protected MapBlockMarker(MapDecorationType<D, ?> type) {
        this.type = type;
    }

    /**
     * load a world marker to nbt. must match saveToNBT
     * implement if you are adding extra data
     */
    public void loadFromNBT(CompoundTag compound) {
        this.pos = NbtUtils.readBlockPos(compound.getCompound("Pos"));
        this.name = compound.contains("Name") ? Component.Serializer.fromJson(compound.getString("Name")) : null;
        this.persistent = compound.getBoolean("Persistent");
    }

    /**
     * save a world marker to nbt. must match the factory function provided to the decoration type
     * implement if you are adding extra data
     *
     * @return nbt
     */
    public CompoundTag saveToNBT() {
        var compound = new CompoundTag();
        if (this.pos != null) {
            compound.put("Pos", NbtUtils.writeBlockPos(this.pos));
        }
        if (this.name != null) {
            compound.putString("Name", Component.Serializer.toJson(this.name));
        }
        if (this.persistent) compound.putBoolean("Persistent", true);
        return compound;
    }

    public boolean shouldRefresh() {
        if (persistent) return false;
        return type.isFromWorld();
    }

    public boolean shouldSave() {
        return persistent || type.isFromWorld();
    }

    /**
     * Forces this to be always saved, disregarding the one from the world
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * implement if you are adding extra data
     * see default example for an implementation
     *
     * @param o another marker object
     * @return true if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapBlockMarker<?> that = (MapBlockMarker<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(pos, that.pos) && Objects.equals(name, that.name);
    }

    /**
     * implement if you are adding extra data
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, pos, name);
    }

    /**
     * ids have to be unique so add here all the data you have in your marker to tell them apart
     *
     * @return suffix
     */
    private String getPosSuffix() {
        return pos == null ? "" : pos.getX() + "," + pos.getY() + "," + pos.getZ();
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

    public void setRotation(int rot) {
        this.rot = rot;
    }

    public float getRotation() {
        return rot;
    }

    public Component getName() {
        return name;
    }

    public void setName(Component name) {
        this.name = name;
    }

    /**
     * creates a decoration given its map position and rotation
     *
     * @param mapX map x position
     * @param mapY map y position
     * @param rot  decoration rotation
     * @return decoration instance
     */
    @NotNull
    protected abstract D doCreateDecoration(byte mapX, byte mapY, byte rot);

    /**
     * Creates a decoration from this marker.
     * This its default vanilla implementation.<br>
     * You can do here extra check for a dimension type and so on.<br>
     * For everything else, just implement doCreateDecoration
     *
     * @return new decoration instance
     */
    @Nullable
    public D createDecorationFromMarker(MapItemSavedData data) {
        BlockPos pos = this.getPos();
        if (pos == null) return null;
        double worldX = pos.getX();
        double worldZ = pos.getZ();
        double rotation = this.getRotation();

        int i = 1 << data.scale;
        float f = (float) (worldX - data.centerX) / i;
        float f1 = (float) (worldZ - data.centerZ) / i;
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


}
