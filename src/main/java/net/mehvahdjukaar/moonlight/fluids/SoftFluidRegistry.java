package net.mehvahdjukaar.moonlight.fluids;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class SoftFluidRegistry {

    private static final ResourceLocation FLUIDS_MAP_KEY = Moonlight.res("fluids_map");
    private static final ResourceLocation ITEMS_MAP_KEY = Moonlight.res("items_map");

    public static final SoftFluid EMPTY = new SoftFluid.Builder(new ResourceLocation(""),
            new ResourceLocation("")).build();

    public static final ResourceKey<Registry<SoftFluid>> REGISTRY_KEY = ResourceKey.createRegistryKey(Moonlight.res("soft_fluids"));
    public static final DeferredRegister<SoftFluid> DEFERRED_REGISTER = DeferredRegister.create(REGISTRY_KEY, REGISTRY_KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<SoftFluid>> SOFT_FLUIDS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<SoftFluid>()
                    .setDefaultKey(Moonlight.res("empty"))
                    .dataPackRegistry(SoftFluid.CODEC, SoftFluid.CODEC)
                    .onCreate(SoftFluidRegistry::onCreate)
                    .onClear(SoftFluidRegistry::onClear)
                    .allowModification()
                    .disableSaving());

    public static HashMap<Fluid, SoftFluid> getFluidsMap() {
        return SOFT_FLUIDS.get().getSlaveMap(FLUIDS_MAP_KEY, HashMap.class);
    }

    public static HashMap<Item, SoftFluid> getItemsMap() {
        return SOFT_FLUIDS.get().getSlaveMap(ITEMS_MAP_KEY, HashMap.class);
    }

    private static Registry<SoftFluid> getDataPackRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(REGISTRY_KEY);
    }

    public static Collection<SoftFluid> getValues() {
        return getDataPackRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<SoftFluid>, SoftFluid>> getEntries() {
        return getDataPackRegistry().entrySet();
    }

    public static SoftFluid get(String id) {
        return get(new ResourceLocation(id));
    }


    @Nullable
    public static ResourceLocation getID(SoftFluid s) {
        return getDataPackRegistry().getKey(s);
    }

    /**
     * gets a soft fluid provided his registry id
     *
     * @param id fluid registry id
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid get(ResourceLocation id) {
        if(id.getNamespace().equals("selene"))id = Moonlight.res(id.getPath()); //backwards compat
        return getDataPackRegistry().get(id);
    }

    public static Optional<SoftFluid> getOptional(ResourceLocation id) {
        return getDataPackRegistry().getOptional(id);
    }

    /**
     * gets a soft fluid provided a forge fluid
     *
     * @param fluid equivalent forge fluid
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromForgeFluid(Fluid fluid) {
        return getFluidsMap().getOrDefault(fluid, EMPTY);
    }

    /**
     * gets a soft fluid provided a bottle like item
     *
     * @param filledContainerItem item containing provided fluid
     * @return soft fluid. empty fluid if not found
     */
    @Nonnull
    public static SoftFluid fromItem(Item filledContainerItem) {
        return getItemsMap().getOrDefault(filledContainerItem, EMPTY);
    }

    public static void onCreate(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        owner.setSlaveMap(FLUIDS_MAP_KEY, new HashMap<>());
        owner.setSlaveMap(ITEMS_MAP_KEY, new HashMap<>());
    }

    public static void onClear(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        owner.getSlaveMap(FLUIDS_MAP_KEY, HashMap.class).clear();
        owner.getSlaveMap(ITEMS_MAP_KEY, HashMap.class).clear();
    }

    private static void addExistingForgeFluids() {
        //only runs on the first object
        var fluidMap = getFluidsMap();
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) getDataPackRegistry();
        reg.unfreeze();
        for (Fluid f : ForgeRegistries.FLUIDS) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it means that another equivalent fluid has already been registered
                if (fluidMap.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid
                if (Utils.getID(f) != null) {
                    SoftFluid sf = (new SoftFluid.Builder(f)).build();
                    //calling vanilla register function because calling that deferred register or forge registry now does nothing
                    //cope
                    //SOFT_FLUIDS.get().register(sf.getRegistryName(),sf);
                    Registry.register(reg, Utils.getID(f), sf);
                    fluidMap.put(f, sf);
                }
            } catch (Exception ignored) {
            }
        }
        //adds empty fluid
        Registry.register(reg, Moonlight.res("empty"),  EMPTY);
        reg.freeze();
    }

    private static void populateSlaveMaps() {
        var itemMap = getItemsMap();
        var fluidsMap = getFluidsMap();
        for (var s : getValues()) {
            if(ModList.get().isLoaded(s.getFromMod())) {
                s.getEquivalentFluids().forEach(f -> fluidsMap.put(f, s));
                s.getContainerList().getPossibleFilled().forEach(i -> {
                    //don't associate water to potion bottle
                    if (i != Items.POTION || !(getID(s).toString().equals("minecraft:water"))) {
                        itemMap.put(i, s);
                    }
                });
            }
        }
    }


    public static void postInitClient() {
        populateSlaveMaps();
        SoftFluidClient.refresh();
    }

    public static void postInitServer() {
        populateSlaveMaps();
        addExistingForgeFluids();
    }
}

