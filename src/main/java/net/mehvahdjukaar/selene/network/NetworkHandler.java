package net.mehvahdjukaar.selene.network;

import net.mehvahdjukaar.selene.Selene;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;


public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static int nextID() { return ID++; }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Selene.MOD_ID, "network"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), SyncCustomMapDecorationPacket.class,SyncCustomMapDecorationPacket::buffer,
                SyncCustomMapDecorationPacket::new, SyncCustomMapDecorationPacket::handler);
    }
}