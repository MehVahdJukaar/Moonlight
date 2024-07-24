package net.mehvahdjukaar.moonlight.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class ClientBoundOnPistonMovedBlockPacket implements Message {
    public final BlockPos pos;
    private final Direction dir;
    private final BlockState movedState;
    private final boolean extending;

    public ClientBoundOnPistonMovedBlockPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dir = Direction.from3DDataValue(buffer.readVarInt());
        this.movedState = buffer.readById(Block.BLOCK_STATE_REGISTRY::byIdOrThrow);
        this.extending = buffer.readBoolean();
    }

    public ClientBoundOnPistonMovedBlockPacket(BlockPos pos, BlockState movedState, Direction direction, boolean extending) {
        this.pos = pos;
        this.movedState = movedState;
        this.dir = direction;
        this.extending = extending;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.dir.get3DDataValue());
        buffer.writeById(Block.BLOCK_STATE_REGISTRY::getIdOrThrow, this.movedState);
        buffer.writeBoolean(this.extending);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        handlePacket(this);
    }

    @Environment(EnvType.CLIENT)
    public static void handlePacket(ClientBoundOnPistonMovedBlockPacket message) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            //Haaack. for some reason this gets received before the block there is actually set so we set it preeemptively
            level.setBlock(message.pos, message.movedState, 0);
            if ( message.movedState.getBlock() instanceof IPistonMotionReact p) {
                p.onMoved(level, message.pos,  message.movedState, message.dir, message.extending);
            }
        }
    }


}