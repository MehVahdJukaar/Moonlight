package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundFinalizeFluidsMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.*;

//TODO: maybe split into api/core?
public class SoftFluidRegistry {

    public static final ResourceKey<Registry<SoftFluid>> KEY = ResourceKey.createRegistryKey(
            Moonlight.res((PlatHelper.getPlatform().isFabric() ? "moonlight/" : "")+ "soft_fluids"));

    public static final ResourceLocation EMPTY_ID = Moonlight.res("empty");

    private static final Map<Fluid, SoftFluid> FLUID_MAP = new IdentityHashMap<>();
    private static final Map<Item, SoftFluid> ITEM_MAP = new IdentityHashMap<>();


    public static SoftFluid getEmpty() {
        return get(EMPTY_ID);
    }

    @ExpectPlatform
    public static void init() {
    }

    public static Registry<SoftFluid> hackyGetRegistry() {
        return Utils.hackyGetRegistry(KEY);
    }

    public static Registry<SoftFluid> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    public static Collection<SoftFluid> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<SoftFluid>, SoftFluid>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    public static SoftFluid get(String id) {
        return get(new ResourceLocation(id));
    }

    /**
     * gets a soft fluid provided his registry id
     *
     * @param id fluid registry id
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid get(ResourceLocation id) {
        String namespace = id.getNamespace();
        Registry<SoftFluid> reg = hackyGetRegistry();
        var r = reg.get(id);
        if (r == null) {
            if (namespace.equals("selene") || namespace.equals("minecraft"))
                id = Moonlight.res(id.getPath()); //backwards compat
            // mc stuff has my id //TODO: split into diff folders for each modded fluid
            r = reg.get(id);
            if(r == null){
                return reg.get(EMPTY_ID); //should not be needed but better be sure
            }
        }
        return r;
    }

    public static Optional<SoftFluid> getOptional(ResourceLocation id) {
        String namespace = id.getNamespace();
        if (namespace.equals("selene") || namespace.equals("minecraft"))
            id = Moonlight.res(id.getPath()); //backwards compat
        return hackyGetRegistry().getOptional(id);
    }

    /**
     * gets a soft fluid provided a forge fluid
     *
     * @param fluid equivalent forge fluid
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromForgeFluid(Fluid fluid) {
        return FLUID_MAP.getOrDefault(fluid, getEmpty());
    }

    /**
     * gets a soft fluid provided a bottle like item
     *
     * @param filledContainerItem item containing provided fluid
     * @return soft fluid. empty fluid if not found
     */
    @Nonnull
    public static SoftFluid fromItem(Item filledContainerItem) {
        return ITEM_MAP.getOrDefault(filledContainerItem, getEmpty());
    }

    //needs to be called on both sides
    private static void populateSlaveMaps() {
        var itemMap = ITEM_MAP;
        itemMap.clear();
        var fluidsMap = FLUID_MAP;
        fluidsMap.clear();
        for (var s : getValues()) {
            if (PlatHelper.isModLoaded(s.getFromMod())) {
                s.getEquivalentFluids().forEach(f -> fluidsMap.put(f, s));
                s.getContainerList().getPossibleFilled().forEach(i -> {
                    //don't associate water to potion bottle
                    if (i != Items.POTION || s != BuiltInSoftFluids.WATER.get()) {
                        itemMap.put(i, s);
                    }
                });
            }
        }
    }


    //wtf is going on here

    //called by data sync to player
    @ApiStatus.Internal
    public static void postInitClient() {
        populateSlaveMaps();
        SoftFluidClient.refreshParticleColors();
    }

    @ApiStatus.Internal
    public static void onDataSyncToPlayer(ServerPlayer player, boolean o) {
        ModMessages.CHANNEL.sendToClientPlayer(player, new ClientBoundFinalizeFluidsMessage());
    }

    //on data load
    @ApiStatus.Internal
    public static void onDataLoad() {
        populateSlaveMaps();
        //registers existing fluids. also update the salve maps
        //TODO: why not needed on the client?
        registerExistingVanillaFluids(FLUID_MAP, ITEM_MAP);
    }

    @ExpectPlatform
    private static void registerExistingVanillaFluids(Map<Fluid, SoftFluid> fluidMap, Map<Item, SoftFluid> itemMap) {
        throw new AssertionError();
    }


}

