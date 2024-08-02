package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class ClientBoundOnPistonMovedBlockPacket implements Message {

    public static final TypeAndCodec<FriendlyByteBuf, ClientBoundOnPistonMovedBlockPacket> TYPE =
            Message.makeType(Moonlight.res("s2c_on_piston_moved_block"), ClientBoundOnPistonMovedBlockPacket::new);

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
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.dir.get3DDataValue());
        buffer.writeById(Block.BLOCK_STATE_REGISTRY::getIdOrThrow, this.movedState);
        buffer.writeBoolean(this.extending);
    }

    @Override
    public void handle(Context context) {
        var level = context.getPlayer().level();
        //Haaack. for some reason this gets received before the block there is actually set so we set it preemptively
        level.setBlock(this.pos, this.movedState, 0);
        if (this.movedState.getBlock() instanceof IPistonMotionReact p) {
            p.onMoved(level, this.pos, this.movedState, this.dir, this.extending);
        }
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}