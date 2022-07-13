package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncMapDecorationTypesMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        SoftFluidRegistryImpl.init(bus);
        Moonlight.commonInit();

        MinecraftForge.EVENT_BUS.register(this);
        /**
         * Update stuff:
         * Configs
         * sand later
         * ash layer
         * leaf layer
         */

        //TODO: fix layers texture generation
        //TODO: fix grass growth replacing double plants and add tag

        bus.addListener(MoonlightForge::registerAdditional);
    }


    public static void registerAdditional(RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) return;
    }

    @SubscribeEvent
    public void onDataLoad(OnDatapackSyncEvent event) {

        // if we're on the server, send syncing packets
        SoftFluidRegistry.postInitServer();
        ModMessages.CHANNEL.sendToAllClientPlayers(
                new ClientBoundSyncMapDecorationTypesMessage(MapDecorationRegistry.DATA_DRIVEN_REGISTRY.getTypes()));

    }
}

