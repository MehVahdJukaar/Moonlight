package net.mehvahdjukaar.moonlight.api.map.type;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Extensible class
 * Represents the actual map marker displayed on a map
 * default base simple decoration. This will be instanced in a map. Equivalent of a tile entity or decorations for maps themselves
 */
public class MLMapDecoration {

    public static final StreamCodec<RegistryFriendlyByteBuf, MLMapDecoration> CODEC =
            MlMapDecorationType.STREAM_CODEC.dispatch(MLMapDecoration::getType,
                    MlMapDecorationType::getDecorationCodec);


    static final StreamCodec<RegistryFriendlyByteBuf, MLMapDecoration> DIRECT_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, MLMapDecoration::getX,
            ByteBufCodecs.BYTE, MLMapDecoration::getY,
            ByteBufCodecs.BYTE, MLMapDecoration::getRot,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, m -> Optional.ofNullable(m.displayName),
            ByteBufCodecs.BOOL, MLMapDecoration::isFromExplorationMap,
            MLMapDecoration::new
    );

    MlMapDecorationType<?, ?> type;
    private Component displayName;
    private byte x;
    private byte y;
    private byte rot;

    boolean isFromExplorationMap;

    public MLMapDecoration(byte x, byte y, byte rot, Optional<Component> displayName, boolean isFromExplorationMap) {
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.displayName = displayName.orElse(null);
        this.isFromExplorationMap = isFromExplorationMap;
    }

    public final MlMapDecorationType<?, ?> getType() {
        return this.type;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRot() {
        return this.rot;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }

    public void setRot(byte rot) {
        this.rot = rot;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public void setY(byte y) {
        this.y = y;
    }

    public void setFromExplorationMap(boolean fromExplorationMap) {
        isFromExplorationMap = fromExplorationMap;
    }

    public boolean isFromExplorationMap() {
        return isFromExplorationMap;
    }

    @Nullable
    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof MLMapDecoration mapDecoration) {
            if (this.type != mapDecoration.type) {
                return false;
            } else if (this.rot != mapDecoration.rot) {
                return false;
            } else if (this.x != mapDecoration.x) {
                return false;
            } else if (this.y != mapDecoration.y) {
                return false;
            } else {
                return Objects.equals(this.displayName, mapDecoration.displayName);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = Utils.getID(this.type).hashCode();
        i = 31 * i + this.x;
        i = 31 * i + this.y;
        i = 31 * i + this.rot;
        return 31 * i + Objects.hashCode(this.displayName);
    }
}

