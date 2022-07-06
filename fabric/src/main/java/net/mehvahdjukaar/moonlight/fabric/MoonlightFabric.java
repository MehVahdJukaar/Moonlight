package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.block_set.fabric.BlockSetManagerImpl;
import net.mehvahdjukaar.moonlight.platform.registry.fabric.RegHelperImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;

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
        BlockSetManagerImpl.registerEntries();
        Moonlight.commonSetup();
    }

    public static MinecraftServer currentServer;

    private void onServerStarting(MinecraftServer minecraftServer) {
        currentServer = minecraftServer;
        MoonlightFabric.onCommonSetup();
    }

}
