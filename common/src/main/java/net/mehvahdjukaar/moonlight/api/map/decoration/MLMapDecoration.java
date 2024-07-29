package net.mehvahdjukaar.moonlight.api.map.decoration;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
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

    // network codec. only send stuff needed on client
    public static final StreamCodec<RegistryFriendlyByteBuf, MLMapDecoration> CODEC =
            MLMapDecorationType.STREAM_CODEC.dispatch(MLMapDecoration::getType,
                    h -> h.value().getDecorationCodec());


    static final StreamCodec<RegistryFriendlyByteBuf, MLMapDecoration> DIRECT_CODEC = StreamCodec.composite(
            MLMapDecorationType.STREAM_CODEC, MLMapDecoration::getType,
            ByteBufCodecs.BYTE, MLMapDecoration::getX,
            ByteBufCodecs.BYTE, MLMapDecoration::getY,
            ByteBufCodecs.BYTE, MLMapDecoration::getRot,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, m -> Optional.ofNullable(m.getDisplayName()),
            MLMapDecoration::new
    );

    private final Holder<MLMapDecorationType<?, ?>> type;
    protected Component displayName;
    protected byte x;
    protected byte y;
    protected byte rot;

    public MLMapDecoration(Holder<MLMapDecorationType<?, ?>> type,
                           byte x, byte y, byte rot, Optional<Component> displayName) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.displayName = displayName.orElse(null);
    }

    public final Holder<MLMapDecorationType<?, ?>> getType() {
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

