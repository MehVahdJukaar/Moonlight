package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record ServerBoundUpdateBoxBlockTileMessage(BlockPos tilePos, BlockPos offset, Vec3i size, String name,
                                                   boolean showBB, String finalState) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundUpdateBoxBlockTileMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_update_box_block"), ServerBoundUpdateBoxBlockTileMessage::new);

    public ServerBoundUpdateBoxBlockTileMessage(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBlockPos(), new Vec3i(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()),
                buf.readUtf(32767), buf.readBoolean(), buf.readUtf(32767));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.tilePos);
        buf.writeBlockPos(this.offset);
        buf.writeVarInt(this.size.getX());
        buf.writeVarInt(this.size.getY());
        buf.writeVarInt(this.size.getZ());
        buf.writeUtf(this.name);
        buf.writeBoolean(this.showBB);
        buf.writeUtf(this.finalState);
    }

    @Override
    public void handle(Context context) {
        // server level
        Player player = context.getPlayer();
        Level level = player.level();
        BlockEntity blockEntity = level.getBlockEntity(this.tilePos);

        if (player.canUseGameMasterBlocks()) {
            if (blockEntity instanceof SpawnBoxBlockEntity boxTile) {
                boxTile.setTargetName(this.name);
                boxTile.setBoxOffset(this.offset);
                boxTile.setBoxSize(this.size);
                boxTile.setShowBoundingBox(this.showBB);
                boxTile.setFinalState(this.finalState);

                boxTile.setChanged();
                BlockState state = boxTile.getBlockState();
                player.level().sendBlockUpdated(this.tilePos, state, state, 3);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
