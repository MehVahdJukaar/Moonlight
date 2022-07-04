package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.minecraftforge.eventbus.api.IEventBus;
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


        Moonlight.commonInit();

        /**
         * Update stuff:
         * Configs
         * sand later
         * ash layer
         * leaf layer
         */

        //TODO: fix layers texture generation
        //TODO: fix grass growth replacing double plants and add tag


        bus.addListener(MoonlightForge::init);
        bus.addListener(MoonlightForge::registerAdditional);
    }


    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Moonlight.commonSetup();
        });
    }


    public static void registerAdditional(RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) return;
        Moonlight.commonRegistration();
    }


}
