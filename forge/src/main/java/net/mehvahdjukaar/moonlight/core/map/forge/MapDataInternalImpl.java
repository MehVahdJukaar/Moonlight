package net.mehvahdjukaar.moonlight.core.map.forge;

import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import vazkii.quark.base.recipe.ingredient.FlagIngredient;


public class MapDataInternalImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MapDataInternal.KEY, MapDataInternal.CODEC, MapDataInternal.NETWORK_CODEC);
    }

    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(MapDataInternalImpl.class);
    }
}