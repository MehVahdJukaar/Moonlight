package net.mehvahdjukaar.moonlight.api.util.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record EnumStreamCodec<T extends Enum<T>>(Class<T> enumClass) implements StreamCodec<FriendlyByteBuf, T> {

    @Override
    public T decode(FriendlyByteBuf buf) {
        return buf.readEnum(this.enumClass);
    }

    @Override
    public void encode(FriendlyByteBuf buf, T e) {
        buf.writeEnum(e);
    }

    @Override
    public @NotNull String toString() {
        return "EnumStreamCodec[" + enumClass.getSimpleName() + "]";
    }
}

