package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.platform.registry.fabric.RegHelperImpl;
import net.minecraft.server.packs.PackType;

public class MoonlightFabric implements ModInitializer {

    public static final String MOD_ID = Moonlight.MOD_ID;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();
        Moonlight.commonSetup();
        Moonlight.commonRegistration();


        RegHelperImpl.registerEntries();


        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(BlockGrowthHandler.getInstance(FabricBlockGrowthManager::new));


        FabricLoader.getInstance().getModContainer(MoonlightFabric.MOD_ID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(Moonlight.res("better_brick_items"), modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Moonlight.res("better_brick_blocks"), modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Moonlight.res("visual_waxed_iron_items"), modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Moonlight.res("biome_tinted_mossy_blocks"), modContainer, ResourcePackActivationType.NORMAL);
        });
    }

}
