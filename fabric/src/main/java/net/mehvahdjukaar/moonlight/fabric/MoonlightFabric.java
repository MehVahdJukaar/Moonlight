package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.fabric.BlockSetInternalImpl;
import net.mehvahdjukaar.moonlight.api.platform.registry.fabric.RegHelperImpl;
import net.minecraft.server.MinecraftServer;

public class MoonlightFabric implements ModInitializer {

    public static final String MOD_ID = Moonlight.MOD_ID;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();
        Moonlight.commonRegistration();

        //TODO: fix this and move at a later stage


        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    }

    public static void onCommonSetup() {
        RegHelperImpl.registerEntries();
        BlockSetInternalImpl.registerEntries();
        Moonlight.commonSetup();
    }

    public static MinecraftServer currentServer;

    private void onServerStarting(MinecraftServer minecraftServer) {
        currentServer = minecraftServer;
        MoonlightFabric.onCommonSetup();
    }

}
