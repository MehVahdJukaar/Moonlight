package net.mehvahdjukaar.moonlight.core.fluid.fabric;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.MappedRegistryAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;

public class SoftFluidInternalImpl {

    public static void init() {
        DynamicRegistries.registerSynced(SoftFluidRegistry.KEY, SoftFluid.CODEC, SoftFluid.CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
    }

    public static void registerExistingVanillaFluids(Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        //only runs on the first object
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) SoftFluidRegistry.hackyGetRegistry();
        ((MappedRegistryAccessor) reg).setFrozen(false);
        for (Fluid f : BuiltInRegistries.FLUID) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it means that another equivalent fluid has already been registered
                if (fluidMap.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid

                SoftFluid sf = (new SoftFluid.Builder(f)).build();
                //calling vanilla register function because calling that deferred register or forge registry now does nothing
                //cope
                //SOFT_FLUIDS.get().register(sf.getRegistryName(),sf);
                Registry.register(reg, Utils.getID(f), sf);
                fluidMap.put(f, reg.getHolder(reg.getId(sf)).orElseThrow());

            } catch (Exception ignored) {
            }
        }
        //adds empty fluid
        //Registry.register(reg, Moonlight.res("empty"), SoftFluidRegistry.EMPTY);
        reg.freeze();
    }

}
