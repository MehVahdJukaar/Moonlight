package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.moonlight.api.platform.registry.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.fabric.BlockSetInternalImpl;
import net.minecraft.server.MinecraftServer;

public class MoonlightFabric implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer {

    public static final String MOD_ID = Moonlight.MOD_ID;

    @Override
    public void onInitialize() {
        Moonlight.commonInit();
        ServerLifecycleEvents.SERVER_STARTING.register(s -> currentServer = s);
    }

    //called after all other mod initialize have been called.
    // we can register extra stuff here that depends on those before client and server common setup is fired
    private void commonSetup() {
        RegHelperImpl.registerEntries();
        BlockSetInternalImpl.registerEntries();

        FabricSetupCallbacks.COMMON_SETUP.forEach(Runnable::run);
    }

    public static MinecraftServer currentServer;

    @Override
    public void onInitializeClient() {
        commonSetup();
        FabricSetupCallbacks.CLIENT_SETUP.forEach(Runnable::run);
    }

    @Override
    public void onInitializeServer() {
        commonSetup();
    }
}
