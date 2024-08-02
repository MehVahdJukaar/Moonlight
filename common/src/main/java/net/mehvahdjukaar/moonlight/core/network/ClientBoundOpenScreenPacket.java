package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


public class ClientBoundOpenScreenPacket implements Message {

    public static final TypeAndCodec<FriendlyByteBuf, ClientBoundOpenScreenPacket> TYPE = Message.makeType(
            Moonlight.res("s2c_open_screen"), ClientBoundOpenScreenPacket::new);

    public final BlockPos pos;
    private final Direction dir;

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dir = Direction.from3DDataValue(buffer.readVarInt());
    }

    public ClientBoundOpenScreenPacket(BlockPos pos, Direction hitFace) {
        this.pos = pos;
        this.dir = hitFace;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.dir.get3DDataValue());
    }

    @Override
    public void handle(Context context) {
        Player player = context.getPlayer();
        Level level = player.level();

        BlockPos pos = this.pos;
        if (level.getBlockEntity(pos) instanceof IScreenProvider tile) {
            tile.openScreen(level, pos, player, this.dir);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}