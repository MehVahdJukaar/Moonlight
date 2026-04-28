package net.mehvahdjukaar.moonlight.core.fluid;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundFinalizeFluidsMessage;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

@ApiStatus.Internal
public class SoftFluidInternal {

    private static final SidedInstance<Map<Fluid, Holder<SoftFluid>>> FLUID_MAP = SidedInstance.of(r -> {
        var m = new IdentityHashMap<Fluid, Holder<SoftFluid>>();
        populateFluidSlaveMap(r, m);
        return m;
    });
    private static final SidedInstance<Map<Item, Holder<SoftFluid>>> ITEM_MAP = SidedInstance.of(r -> {
        var m = new IdentityHashMap<Item, Holder<SoftFluid>>();
        populateItemSlaveMap(r, m);
        return m;
    });


    public static Holder<SoftFluid> fromVanillaFluid(Fluid fluid, HolderLookup.Provider registryAccess) {
        return FLUID_MAP.get(registryAccess).get(fluid);
    }

    public static Holder<SoftFluid> fromVanillaItem(Item item, HolderLookup.Provider registryAccess) {
        return ITEM_MAP.get(registryAccess).get(item);
    }

    //needs to be called on both sides
    private static void populateFluidSlaveMap(HolderLookup.Provider registryAccess,
                                          Map<Fluid, Holder<SoftFluid>> fluidMap) {
        fluidMap.clear();
        for (var h : SoftFluidRegistry.get(registryAccess).listElements().toList()) {
            var s = h.value();
            if (s.isEnabled()) {
                for (var eq : s.getEquivalentFluids()) {
                    Fluid value = eq.value();
                    if (value == Fluids.EMPTY) {
                        Moonlight.LOGGER.error("!!Invalid fluid for fluid. This is a bug! {}", h);
                        if (PlatHelper.isDev())
                            throw new AssertionError("Invalid fluid for fluid. This is a bug! " + h);
                    }
                    fluidMap.put(value, h);
                }
                s.getEquivalentFluids().forEach(f -> fluidMap.put(f.value(), h));
            }
        }
    }

    //needs to be called on both sides
    private static void populateItemSlaveMap(HolderLookup.Provider registryAccess,
                                              Map<Item, Holder<SoftFluid>> itemMap) {
        itemMap.clear();
        for (var h : SoftFluidRegistry.get(registryAccess).listElements().toList()) {
            var s = h.value();
            if (s.isEnabled()) {
                s.getContainerList().getPossibleFilled().forEach(i -> {
                    //don't associate water to potion bottle
                    if (i != Items.POTION || !MLBuiltinSoftFluids.WATER.is(h)) {
                        if (i == Items.AIR) {
                            Moonlight.LOGGER.error("!!Invalid item for fluid. This is a bug! {}", h);
                            if (PlatHelper.isDev())
                                throw new AssertionError("Invalid item for fluid. This is a bug! " + h);
                        }
                        itemMap.put(i, h);
                    }
                });
            }
        }
    }


    public static void init() {
        RegHelper.registerDataPackRegistry(
                SoftFluidRegistry.KEY,
                SoftFluid.CODEC,
                SoftFluid.CODEC
        );
    }

    //wtf is going on here

    //called by data sync to player
    public static void postInitClient(RegistryAccess ra) {
        // populate maps
        FLUID_MAP.get(ra);
        ITEM_MAP.get(ra);

        var reg = SoftFluidRegistry.get(ra);
        for (var f : reg) {
            f.afterInit();
        }
        //ok so here the extra registered fluids should have already been sent to the client
        SoftFluidColors.refreshParticleColors(reg);
    }

    public static void onDataSyncToPlayer(ServerPlayer player, boolean isJoined) {
        //just sends on login
        if (isJoined) {
            NetworkHelper.sendToClientPlayer(player, new ClientBoundFinalizeFluidsMessage());
        }
    }

    //on data load
    public static void doPostInitServer(RegistryAccess ra) {
        FLUID_MAP.get(ra);
        ITEM_MAP.get(ra);
        //registers existing fluids. also update the salve maps
        //we need to call this on bont server and client as this happens too late and these wont be sent
        registerExistingVanillaFluids(ra, FLUID_MAP.get(ra), ITEM_MAP.get(ra));

        for (var f : SoftFluidRegistry.get(ra)) {
            f.afterInit();
        }
    }

    @PlatformImpl
    private static void registerExistingVanillaFluids(RegistryAccess ra, Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        throw new AssertionError();
    }


}

