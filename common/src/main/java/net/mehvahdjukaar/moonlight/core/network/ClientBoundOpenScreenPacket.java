package net.mehvahdjukaar.moonlight.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;


public record ClientBoundOpenScreenPacket(BlockPos pos, Direction dir) implements Message {

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), Direction.from3DDataValue(buffer.readVarInt()));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.dir.get3DDataValue());
    }

    @Override
    public void handle(NetworkHelper.Context context) {
        handleOpenScreenPacket(this);
    }

    @Environment(EnvType.CLIENT)
    public static void handleOpenScreenPacket(ClientBoundOpenScreenPacket message) {
        var level = Minecraft.getInstance().level;
        var p = Minecraft.getInstance().player;
        if (level != null && p != null) {
            BlockPos pos = message.pos;
            if (level.getBlockEntity(pos) instanceof IScreenProvider tile) {
                tile.openScreen(level, pos, p, message.dir);
            }
        }
    }


}