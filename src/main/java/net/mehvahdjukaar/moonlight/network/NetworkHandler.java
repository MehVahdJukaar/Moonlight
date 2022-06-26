package net.mehvahdjukaar.moonlight.network;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Moonlight.MOD_ID, "network"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncCustomMapDecorationPacket.class, ClientBoundSyncCustomMapDecorationPacket::buffer,
                ClientBoundSyncCustomMapDecorationPacket::new, ClientBoundSyncCustomMapDecorationPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncFluidsPacket.class, ClientBoundSyncFluidsPacket::buffer,
                ClientBoundSyncFluidsPacket::new, ClientBoundSyncFluidsPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncMapDecorationTypesPacket.class, ClientBoundSyncMapDecorationTypesPacket::buffer,
                ClientBoundSyncMapDecorationTypesPacket::new, ClientBoundSyncMapDecorationTypesPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncModConfigsPacket.class, SyncModConfigsPacket::buffer,
                SyncModConfigsPacket::new, SyncModConfigsPacket::handler);
    }
}