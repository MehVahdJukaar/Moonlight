package net.mehvahdjukaar.moonlight.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableBiMap;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Minecraft
        ForgeHooksClient.getGuiFarPlane()


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
        bus.addGenericListener(Item.class, MoonlightForge::registerAdditional);
    }



    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            Moonlight.commonSetup();
        });
    }


    public static void registerAdditional(RegistryEvent.Register<Item> event){
        Moonlight.commonRegistration();
    }


}
