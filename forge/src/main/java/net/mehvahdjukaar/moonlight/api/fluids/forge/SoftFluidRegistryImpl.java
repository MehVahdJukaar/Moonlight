package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.forge.MoonlightForge;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Map;

import static net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry.KEY;

public class SoftFluidRegistryImpl {

    public static void init() {
        IEventBus bus = MoonlightForge.getCurrentModBus();
        bus.register(SoftFluidRegistryImpl.class);
    }

    public static void registerExistingVanillaFluids(Map<Fluid, SoftFluid> fluidMap, Map<Item, SoftFluid> itemMap) {
        //only runs on the first object
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) SoftFluidRegistry.hackyGetRegistry();
        reg.unfreeze();
        for (Fluid f : BuiltInRegistries.FLUID) {
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
                fluidMap.put(f, sf);
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
