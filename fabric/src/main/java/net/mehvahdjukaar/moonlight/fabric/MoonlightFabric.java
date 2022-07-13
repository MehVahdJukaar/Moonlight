package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.moonlight.api.platform.fabric.PlatformHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.fabric.BlockSetInternalImpl;
import net.mehvahdjukaar.moonlight.api.platform.registry.fabric.RegHelperImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

public class MoonlightFabric implements ModInitializer {

    public static final String MOD_ID = Moonlight.MOD_ID;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarting);
    }
    //onCommon setup only runs once. either for dedicated server through server starting or on client starting
    private void onClientStarting(Minecraft minecraft){
        MoonlightFabric.onCommonSetup();


    }

    private void onServerStarting(MinecraftServer minecraftServer) {
        currentServer = minecraftServer;
        if(minecraftServer instanceof DedicatedServer) MoonlightFabric.onCommonSetup();
    }

    public static void onCommonSetup() {
        RegHelperImpl.registerEntries();
        BlockSetInternalImpl.registerEntries();

        PlatformHelperImpl.invokeCommonSetup();
    }

    public static MinecraftServer currentServer;



}
