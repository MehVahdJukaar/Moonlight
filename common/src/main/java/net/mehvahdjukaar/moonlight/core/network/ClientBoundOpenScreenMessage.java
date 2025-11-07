package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class ClientBoundOpenScreenMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundOpenScreenMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_open_screen"), ClientBoundOpenScreenMessage::new);

    public final TileOrEntityTarget target;
    private final Direction dir;

    public ClientBoundOpenScreenMessage(RegistryFriendlyByteBuf buffer) {
        this.target = TileOrEntityTarget.read(buffer);
        this.dir = Direction.from3DDataValue(buffer.readVarInt());
    }

    public ClientBoundOpenScreenMessage(TileOrEntityTarget target, @Nullable Direction hitFace) {
        this.target = target;
        this.dir = hitFace == null ? Direction.UP : hitFace;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        this.target.write(buffer);
        buffer.writeVarInt(this.dir.get3DDataValue());
    }

    @Override
    public void handle(Context context) {
        Player player = context.getPlayer();
        Level level = player.level();

        if (this.target.getTarget(level) instanceof IScreenProvider tile) {
            tile.openScreen(level, player, this.dir);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}