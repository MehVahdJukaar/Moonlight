package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * represents a block tracker instance which keeps track of a placed block and creates its associated map decoration
 *
 * @param <D> decoration
 */
public abstract class MLMapMarker<D extends MLMapDecoration> {
    //Static ref is fine as data registry cant change with reload command. Just don't hold ref outside of a world

    private final Holder<MLMapDecorationType<?, ?>> type;
    @NotNull
    protected final BlockPos pos;
    protected final float rot;
    protected final Optional<Component> name;

    protected final boolean preventsExtending;
    protected final boolean shouldRefresh;
    protected final boolean shouldSave;

    public static final Codec<MLMapMarker<?>> CODEC =
            MLMapDecorationType.CODEC.dispatch("type", MLMapMarker::getType,
                    mapWorldMarker -> mapWorldMarker.value().getMarkerCodec());

    public static <T extends MLMapMarker<?>> Products.P7<RecordCodecBuilder.Mu<T>, Holder<MLMapDecorationType<?, ?>>, BlockPos, Float, Optional<Component>, Optional<Boolean>, Optional<Boolean>, Boolean> baseCodecGroup(
            RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                MLMapDecorationType.CODEC.fieldOf("type").forGetter(m -> m.getType()),
                BlockPos.CODEC.fieldOf("pos").forGetter(m -> m.getPos()),
                Codec.FLOAT.optionalFieldOf("rot", 0f).forGetter(m -> m.getRotation()),
                ComponentSerialization.FLAT_CODEC.optionalFieldOf("name").forGetter(m -> m.getDisplayName()),
                Codec.BOOL.optionalFieldOf("should_refresh").forGetter(m -> Optional.of(m.shouldRefreshFromWorld())),
                Codec.BOOL.optionalFieldOf("should_save").forGetter(m -> Optional.of(m.shouldSave())),
                Codec.BOOL.optionalFieldOf("prevents_extending", false).forGetter(m -> m.preventsExtending())
        );
    }

    public MLMapMarker(Holder<MLMapDecorationType<?, ?>> type, BlockPos pos,
                       float rotation, Optional<Component> component,
                       Optional<Boolean> shouldRefresh, Optional<Boolean> shouldSave, boolean preventsExtending) {
        this.type = type;
        this.pos = pos;
        this.rot = rotation;
        this.name = component;

        this.shouldRefresh = shouldRefresh.orElse(type.value().isFromWorld());
        this.shouldSave = shouldSave.orElse(type.value().isFromWorld());
        this.preventsExtending = preventsExtending;
    }

    public Holder<MLMapDecorationType<?, ?>> getType() {
        return type;
    }

    public boolean shouldRefreshFromWorld() {
        return shouldRefresh;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    //TODO: add flag system instead?
    public boolean preventsExtending() {
        return false;
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
        MLMapMarker<?> that = (MLMapMarker<?>) o;
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
    public String getMarkerUniqueId() {
        return this.type.getRegisteredName() + "-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public float getRotation() {
        return rot;
    }

    public Optional<Component> getDisplayName() {
        return name;
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

    // override to give special behaviors
    public int getFlags() {
        return 0;
    }

    public boolean hasFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    public static final int HAS_SMALL_TEXTURE_FLAG = 1;

}
