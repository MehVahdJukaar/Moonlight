package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.core.misc.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.CompatWoodTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean HAS_BEEN_INIT = true;

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        CompatWoodTypes.init();
        ModMessages.registerMessages();
        VillagerAIInternal.init();
        ModCriteriaTriggers.register();
        SoftFluidRegistry.init();
        MapDecorationRegistry.init();


        //client init
        if (PlatformHelper.getEnv().isClient()) {

            ClientPlatformHelper.addAtlasTextureCallback(TextureAtlas.LOCATION_BLOCKS, e -> {
                SoftFluidClient.getTexturesToStitch().forEach(e::addSprite);
            });

            ClientPlatformHelper.addClientReloadListener(new SoftFluidClient(), res("soft_fluids"));

            ClientDynamicResourcesHandler.INSTANCE.register();
        }
    }

    public static class ClientDynamicResourcesHandler extends DynClientResourcesProvider {

        public static final ClientDynamicResourcesHandler INSTANCE = new ClientDynamicResourcesHandler();

        public ClientDynamicResourcesHandler() {
            super(new DynamicTexturePack(Moonlight.res("generated_pack")));
            this.dynamicPack.generateDebugResources = PlatformHelper.isDev();
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        @Override
        public boolean dependsOnLoadedPacks() {
            return true;
        }

        @Override
        public void regenerateDynamicAssets(ResourceManager manager) {

        }
    }


    }
