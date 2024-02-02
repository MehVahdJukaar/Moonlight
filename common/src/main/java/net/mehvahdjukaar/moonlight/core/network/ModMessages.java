package net.mehvahdjukaar.moonlight.core.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;


public class ModMessages {

    public static final ResourceLocation SPAWN_PACKET_ID = Moonlight.res("0");

    @ExpectPlatform
    private static void loaderDependant(NetworkHelper.RegEvent builder) {
    }

    public static void init() {
        NetworkHelper.addRegistration(Moonlight.MOD_ID, event -> {
            event.setVersion(5);

            event.and(ModMessages::loaderDependant);

            event.register(NetworkDir.CLIENTBOUND,
                    ClientBoundFinalizeFluidsMessage.class, ClientBoundFinalizeFluidsMessage::new);

            event.register(NetworkDir.CLIENTBOUND,
                    ClientBoundSyncConfigsMessage.class, ClientBoundSyncConfigsMessage::new);

            event.register(NetworkDir.CLIENTBOUND,
                    ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::new);

            event.register(NetworkDir.CLIENTBOUND,
                    ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::new);
        });
    }

}