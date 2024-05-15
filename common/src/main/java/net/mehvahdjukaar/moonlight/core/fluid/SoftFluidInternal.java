package net.mehvahdjukaar.moonlight.core.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundFinalizeFluidsMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

import static net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry.getHolders;

@ApiStatus.Internal
public class SoftFluidInternal {

    public static final Map<Fluid, Holder<SoftFluid>> FLUID_MAP = new IdentityHashMap<>();
    public static final Map<Item, Holder<SoftFluid>> ITEM_MAP = new IdentityHashMap<>();

    //needs to be called on both sides
    private static void populateSlaveMaps() {
        var itemMap = ITEM_MAP;
        itemMap.clear();
        var fluidsMap = FLUID_MAP;
        fluidsMap.clear();
        for (var h : getHolders()) {
            var s = h.value();
            if (s.isEnabled()) {
                s.getEquivalentFluids().forEach(f -> fluidsMap.put(f, h));
                s.getContainerList().getPossibleFilled().forEach(i -> {
                    //don't associate water to potion bottle
                    if (i != Items.POTION || s != BuiltInSoftFluids.WATER.get()) {
                        itemMap.put(i, h);
                    }
                });
            }
        }
    }


    @ExpectPlatform
    public static void init() {
    }

    //wtf is going on here

    //called by data sync to player
    public static void postInitClient() {
        populateSlaveMaps();
        //ok so here the extra registered fluids should have already been sent to the client
    }

    public static void onDataSyncToPlayer(ServerPlayer player, boolean isJoined) {
        //just sends on login
        if(isJoined) {
            ModMessages.CHANNEL.sendToClientPlayer(player, new ClientBoundFinalizeFluidsMessage());
        }
    }

    //on data load
    public static void doPostInitServer() {
        populateSlaveMaps();
        //registers existing fluids. also update the salve maps
        //we need to call this on bont server and client as this happens too late and these wont be sent
        registerExistingVanillaFluids(FLUID_MAP, ITEM_MAP);
    }

    @ExpectPlatform
    private static void registerExistingVanillaFluids(Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        throw new AssertionError();
    }


}

