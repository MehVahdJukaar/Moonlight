package net.mehvahdjukaar.moonlight.core.fluid.platform;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.mixins.platform.HolderReferenceInvoker;
import net.mehvahdjukaar.moonlight.core.mixins.platform.MappedRegistryAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;

public class SoftFluidInternalImpl {

    public static void registerExistingVanillaFluids(RegistryAccess ra, Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        //only runs on the first object
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) SoftFluidRegistry.get(ra);
        ((MappedRegistryAccessor) reg).setFrozen(false);
        for (Fluid f : BuiltInRegistries.FLUID) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it means that another equivalent fluid has already been registered
                if (fluidMap.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid

                SoftFluid sf = (new SoftFluid(BuiltInRegistries.FLUID.wrapAsHolder(f)));
                //calling vanilla register function because calling that deferred register or forge registry now does nothing
                //cope
                //SOFT_FLUIDS.get().register(sf.getRegistryName(),sf);
                var holder = reg.register(ResourceKey.create(SoftFluidRegistry.KEY, Utils.getID(f)), sf,
                        RegistrationInfo.BUILT_IN);
                //freeze() cant run again once tags are bound
                ((HolderReferenceInvoker) holder).invokeBindValue(sf);
                holder.bindComponents(DataComponentMap.EMPTY);
                fluidMap.put(f, holder);

            } catch (Exception ignored) {
            }
        }
        ((MappedRegistryAccessor) reg).setFrozen(true);

    }

}
