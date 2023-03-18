package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SoftFluidRegistryImpl {

    private static final ResourceLocation FLUIDS_MAP_KEY = Moonlight.res("fluids_map");
    private static final ResourceLocation ITEMS_MAP_KEY = Moonlight.res("items_map");

    public static final ResourceKey<Registry<SoftFluid>> KEY = ResourceKey.createRegistryKey(Moonlight.res("soft_fluids"));

    public static final DeferredRegister<SoftFluid> DEFERRED_REGISTER = DeferredRegister.create(KEY, KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<SoftFluid>> SOFT_FLUIDS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<SoftFluid>()
                    .setDefaultKey(Moonlight.res("empty"))
                    .onCreate(SoftFluidRegistryImpl::onCreate)
                    .onClear(SoftFluidRegistryImpl::onClear)
                    .allowModification()
                    .disableSaving());

    //do not reference. will cause problem on client
    private static final RegistryObject<SoftFluid> EMPTY = DEFERRED_REGISTER.register(SoftFluidRegistry.EMPTY_ID.getPath(),
            () -> new SoftFluid.Builder(new ResourceLocation(""),
                    new ResourceLocation("")).build());

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(SoftFluidRegistryImpl::registerDataPackRegistry);
        DEFERRED_REGISTER.register(bus);
    }

    public static ResourceKey<Registry<SoftFluid>> getRegistryKey() {
        return KEY;
    }

    public static Map<Fluid, SoftFluid> getFluidsMap() {
        return SOFT_FLUIDS.get().getSlaveMap(FLUIDS_MAP_KEY, HashMap.class);
    }

    public static Map<Item, SoftFluid> getItemsMap() {
        return SOFT_FLUIDS.get().getSlaveMap(ITEMS_MAP_KEY, HashMap.class);
    }

    public static void onCreate(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        owner.setSlaveMap(FLUIDS_MAP_KEY, new IdentityHashMap<>());
        owner.setSlaveMap(ITEMS_MAP_KEY, new IdentityHashMap<>());
    }

    public static void onClear(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        owner.getSlaveMap(FLUIDS_MAP_KEY, IdentityHashMap.class).clear();
        owner.getSlaveMap(ITEMS_MAP_KEY, IdentityHashMap.class).clear();
    }

    public static void registerExistingVanillaFluids() {
        //only runs on the first object
        var fluidMap = getFluidsMap();
        MappedRegistry<SoftFluid> reg = (MappedRegistry<SoftFluid>) SoftFluidRegistry.getDataPackRegistry();
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
                fluidMap.put(f, sf);
            } catch (Exception ignored) {
            }
        }
        reg.freeze();
    }

    @EventCalled
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KEY, SoftFluid.CODEC, SoftFluid.CODEC);
    }
}
