import io.netty.channel.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class ChannelHandlerExample {

    // Just loads the class
    public static void init() {
        NetworkHelper.addNetworkRegistration(ChannelHandlerExample::registerMessages, 1);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(S2CTestMessage.TYPE);
    }

    // Showcasing usage of network handler
    public static void sendMessageToPlayers(ServerLevel level, BlockPos pos, int range, int data) {
        NetworkHelper.sendToAllClientPlayersInRange(level, pos, range, new S2CTestMessage(data));
    }


    public record S2CTestMessage(int data) implements Message {

        public static final TypeAndCodec<RegistryFriendlyByteBuf, S2CTestMessage> TYPE = Message.makeType(
                Moonlight.res("s2c_test"), S2CTestMessage::new);

        public S2CTestMessage(FriendlyByteBuf buffer) {
            this(buffer.readVarInt());
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(this.data);
        }

        @Override
        public void handle(Context context) {
            // Handle your packet on the client here. Be mindful of classloading tho
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE.type();
        }
    }
}
