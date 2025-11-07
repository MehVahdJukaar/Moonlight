package net.mehvahdjukaar.moonlight.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.valueproviders.UniformInt;

public class ClientBoundParticleAroundBlockMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundParticleAroundBlockMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_particle"), ClientBoundParticleAroundBlockMessage::new);

    public final Kind type;
    public final BlockPos pos;

    public ClientBoundParticleAroundBlockMessage(RegistryFriendlyByteBuf buffer) {
        this.type = buffer.readEnum(Kind.class);
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundParticleAroundBlockMessage(BlockPos pos, Kind type) {
        this.pos = pos;
        this.type = type;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(Context context) {
        handleSpawnBlockParticlePacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }

    public enum Kind {
        WAX_ON,
        GLOW_ON
    }

    @Environment(EnvType.CLIENT)
    private static void handleSpawnBlockParticlePacket(ClientBoundParticleAroundBlockMessage message) {
        var l = Minecraft.getInstance().level;

        switch (message.type) {
            case WAX_ON -> {
                ParticleUtil.spawnParticleOnBlockShape(l, message.pos,
                        ParticleTypes.WAX_ON,
                        UniformInt.of(3, 5), 0.01f);
            }
            case GLOW_ON -> {
                ParticleUtil.spawnParticleOnBlockShape(l, message.pos,
                        ParticleTypes.GLOW,
                        UniformInt.of(3, 5), 0);
            }
        }
    }


}
