package net.mehvahdjukaar.selene.fluids;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.network.ClientBoundSyncFluidsPacket;
import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class SoftFluidRegistry extends SimpleJsonResourceReloadListener {

    public static final SoftFluidRegistry INSTANCE = new SoftFluidRegistry();

    // id -> SoftFluid
    private final HashMap<ResourceLocation, SoftFluid> idMap = new HashMap<>();
    // filled item -> SoftFluid. need to handle potions separately since they map to same item id
    private final HashMap<Item, SoftFluid> itemMap = new HashMap<>();
    // forge fluid  -> SoftFluid
    private final HashMap<Fluid, SoftFluid> fluidMap = new HashMap<>();
    //for stuff that is registers using a code built fluid
    private boolean initializedDispenser = false;
    private int currentReload = 0;

    private SoftFluidRegistry() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "soft_fluids");
    }


    public static Collection<SoftFluid> getRegisteredFluids() {
        return INSTANCE.idMap.values();
    }

    public static SoftFluid get(String id) {
        return INSTANCE.idMap.getOrDefault(new ResourceLocation(id), EMPTY);
    }

    /**
     * gets a soft fluid provided his registry id
     *
     * @param id fluid registry id
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid get(ResourceLocation id) {
        return INSTANCE.idMap.getOrDefault(id, EMPTY);
    }

    public static Optional<SoftFluid> getOptional(ResourceLocation id) {
        return Optional.ofNullable(INSTANCE.idMap.getOrDefault(id, null));
    }

    /**
     * gets a soft fluid provided a forge fluid
     *
     * @param fluid equivalent forge fluid
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromForgeFluid(Fluid fluid) {
        return INSTANCE.fluidMap.getOrDefault(fluid, EMPTY);
    }

    /**
     * gets a soft fluid provided a bottle like item
     *
     * @param filledContainerItem item containing provided fluid
     * @return soft fluid. empty fluid if not found
     */
    @Nonnull
    public static SoftFluid fromItem(Item filledContainerItem) {
        return INSTANCE.itemMap.getOrDefault(filledContainerItem, EMPTY);
    }

    //vanilla built-in fluid references
    public static final SoftFluid EMPTY = SoftFluid.EMPTY;
    public static final FluidReference WATER = FluidReference.of("minecraft:water");
    public static final FluidReference LAVA = FluidReference.of("minecraft:lava");
    public static final FluidReference HONEY = FluidReference.of("minecraft:honey");
    public static final FluidReference MILK = FluidReference.of("minecraft:milk");
    public static final FluidReference MUSHROOM_STEW = FluidReference.of("minecraft:mushroom_stew");
    public static final FluidReference BEETROOT_SOUP = FluidReference.of("minecraft:beetroot_stew");
    public static final FluidReference RABBIT_STEW = FluidReference.of("minecraft:rabbit_stew");
    public static final FluidReference SUS_STEW = FluidReference.of("minecraft:suspicious_stew");
    public static final FluidReference POTION = FluidReference.of("minecraft:potion");
    public static final FluidReference DRAGON_BREATH = FluidReference.of("minecraft:dragon_breath");
    public static final FluidReference XP = FluidReference.of("minecraft:experience");
    public static final FluidReference SLIME = FluidReference.of("minecraft:slime");
    public static final FluidReference GHAST_TEAR = FluidReference.of("minecraft:ghast_tear");
    public static final FluidReference MAGMA_CREAM = FluidReference.of("minecraft:magma_cream");
    public static final FluidReference POWDERED_SNOW = FluidReference.of("minecraft:powder_snow");


    private static void register(SoftFluid s) {
        if (ModList.get().isLoaded(s.getRegistryName().getNamespace())) {
            for (Fluid f : s.getEquivalentFluids()) {
                //remove non-custom equivalent forge fluids in favor of this one
                if (INSTANCE.fluidMap.containsKey(f)) {
                    SoftFluid old = INSTANCE.fluidMap.get(f);
                    if (!old.isGenerated) {
                        INSTANCE.idMap.remove(old.getRegistryName());
                        old.getFilledContainer(Items.BUCKET).ifPresent(INSTANCE.itemMap::remove);
                    }
                }
            }
            registerUnchecked(s);
        }
    }

    private static void registerUnchecked(SoftFluid... fluids) {
        Arrays.stream(fluids).forEach(s -> {
            s.getEquivalentFluids().forEach(f -> INSTANCE.fluidMap.put(f, s));
            s.getContainerList().getPossibleFilled().forEach(i -> {
                //dont associate water to potion bottle
                if (i != Items.POTION || !s.getRegistryName().toString().equals("minecraft:water")) {
                    INSTANCE.itemMap.put(i, s);
                }
            });
            ResourceLocation key = s.getRegistryName();
            if (INSTANCE.idMap.containsKey(key)) {
                INSTANCE.idMap.put(key, SoftFluid.create(INSTANCE.idMap.get(key), s));
            } else {
                INSTANCE.idMap.put(key, s);
            }
        });
    }

    private static void convertAndRegisterAllForgeFluids() {
        for (Fluid f : ForgeRegistries.FLUIDS) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it means that another equivalent fluid has already been registered
                if (INSTANCE.fluidMap.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid
                if(f.getRegistryName() != null) registerUnchecked((new SoftFluid.Builder(f)).build());
            } catch (Exception ignored) {
            }
        }
    }

    public static void acceptClientFluids(ClientBoundSyncFluidsPacket packet) {
        INSTANCE.idMap.clear();
        INSTANCE.fluidMap.clear();
        INSTANCE.itemMap.clear();
        packet.getFluids().forEach(SoftFluidRegistry::register);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.currentReload++;
        this.idMap.clear();
        this.fluidMap.clear();
        this.itemMap.clear();

        for (var j : jsons.entrySet()) {
            Optional<SoftFluid> result = SoftFluid.CODEC.parse(JsonOps.INSTANCE, j.getValue())
                    .resultOrPartial(e -> Selene.LOGGER.error("Failed to parse soft fluid JSON object for {} : {}", j.getKey(), e));
            result.ifPresent(SoftFluidRegistry::register);
        }
        convertAndRegisterAllForgeFluids();
        if (!this.initializedDispenser) {
            this.initializedDispenser = true;
            getRegisteredFluids().forEach(DispenserHelper::registerFluidBehavior);
        }
        Selene.LOGGER.info("Registered {} Soft Fluids", this.fluidMap.size());
    }


    /**
     * Use these to store fluids instances. They are similar to vanilla holders since they need to refresh after data is reloaded
     */
    public static class FluidReference implements Supplier<SoftFluid> {

        public static FluidReference of(String name) {
            return new FluidReference(new ResourceLocation(name));
        }

        public static FluidReference of(ResourceLocation name) {
            return new FluidReference(name);
        }

        private final Object lock = new Object();
        private SoftFluid value;
        private final ResourceLocation id;
        private int reloadNumber = -1;

        private FluidReference(ResourceLocation id) {
            this.value = null;
            this.id = id;
        }

        @Override
        public SoftFluid get() {
            synchronized (lock) {
                if (INSTANCE.currentReload != this.reloadNumber) {
                    this.reloadNumber = INSTANCE.currentReload;
                    this.value = SoftFluidRegistry.get(id);
                    if (this.value.isEmpty()) {
                        //client might call this during server reload. remeber on single player this is the only object for both "sides"
                        //we return empty so even client during reload can be fine
                        //throw new UnsupportedOperationException("Soft Fluid not present: " + this.id);
                    }
                }
                return value;
            }
        }
    }


}
