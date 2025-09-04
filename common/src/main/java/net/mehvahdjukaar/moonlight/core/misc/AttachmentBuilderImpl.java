package net.mehvahdjukaar.moonlight.core.misc;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class AttachmentBuilderImpl<A> implements RegHelper.AttachmentBuilder<A> {

    public final Supplier<A> initializer;
    @Nullable
    public Codec<A> persistentCodec = null;
    public boolean copyOnDeath = false;
    public Pair<StreamCodec<? super RegistryFriendlyByteBuf, A>, BiPredicate<Object, ServerPlayer>> sync = null;

    public AttachmentBuilderImpl(Supplier<A> initializer) {
        this.initializer = initializer;
    }

    @Override
    public RegHelper.AttachmentBuilder<A> persistent(Codec<A> codec) {
        this.persistentCodec = codec;
        return this;
    }

    @Override
    public RegHelper.AttachmentBuilder<A> copyOnDeath() {
        this.copyOnDeath = true;
        return this;
    }

    @Override
    public RegHelper.AttachmentBuilder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> packetCodec,
                                                   BiPredicate<Object, ServerPlayer> syncPredicate) {
        this.sync = Pair.of(packetCodec, syncPredicate);
        return this;
    }

}