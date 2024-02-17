package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

import static net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry.KEY;

public class SoftFluidRegistryImpl {

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(SoftFluidRegistryImpl.class);
    }

    public static void registerExistingVanillaFluids(Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        //only runs on the first object
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) SoftFluidRegistry.hackyGetRegistry();
        reg.unfreeze();
        for (Fluid f : ForgeRegistries.FLUIDS) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it means that another equivalent fluid has already been registered
                if (fluidMap.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid
                Utils.getID(f);
                SoftFluid sf = (new SoftFluid.Builder(f)).build();
                //calling vanilla register function because calling that deferred register or forge registry now does nothing
                //cope
                //SOFT_FLUIDS.get().register(sf.getRegistryName(),sf);
                Registry.register(reg, Utils.getID(f), sf);

                fluidMap.put(f, reg.getHolder(reg.getId(sf)).orElseThrow());
            } catch (Exception ignored) {
            }
        }
        reg.freeze();
    }

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
      event.dataPackRegistry(KEY, SoftFluid.CODEC, SoftFluid.CODEC);
    }


}
