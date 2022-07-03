package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.block_set.fabric.BlockSetManagerImpl;
import net.mehvahdjukaar.moonlight.platform.registry.fabric.RegHelperImpl;
import net.minecraft.server.packs.PackType;

public class MoonlightFabric implements ModInitializer {

    public static final String MOD_ID = Moonlight.MOD_ID;

    public static void onCommonSetup() {
        BlockSetManagerImpl.registerEntries();
    }

    @Override
    public void onInitialize() {

        Moonlight.commonInit();
        Moonlight.commonSetup();
        Moonlight.commonRegistration();


        RegHelperImpl.registerEntries();
    }

}
