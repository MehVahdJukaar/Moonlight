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
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class SoftFluidRegistry extends SimpleJsonResourceReloadListener {
    // id -> SoftFluid
    private static final HashMap<ResourceLocation, SoftFluid> ID_MAP = new HashMap<>();
    // filled item -> SoftFluid. need to handle potions separately since they map to same item id
    private static final HashMap<Item, SoftFluid> ITEM_MAP = new HashMap<>();
    // forge fluid  -> SoftFluid
    private static final HashMap<Fluid, SoftFluid> FLUID_MAP = new HashMap<>();
    //for stuff that is registers using a code built fluid

    public static Collection<SoftFluid> getRegisteredFluids() {
        return ID_MAP.values();
    }

    public static SoftFluid get(String id) {
        return ID_MAP.getOrDefault(new ResourceLocation(id), EMPTY);
    }

    /**
     * gets a soft fluid provided his registry id
     *
     * @param id fluid registry id
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid get(ResourceLocation id) {
        return ID_MAP.getOrDefault(id, EMPTY);
    }

    public static Optional<SoftFluid> getOptional(ResourceLocation id) {
        return Optional.ofNullable(ID_MAP.getOrDefault(id, null));
    }

    /**
     * gets a soft fluid provided a forge fluid
     *
     * @param fluid equivalent forge fluid
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromForgeFluid(Fluid fluid) {
        return FLUID_MAP.getOrDefault(fluid, EMPTY);
    }

    /**
     * gets a soft fluid provided a bottle like item
     *
     * @param filledContainerItem item containing provided fluid
     * @return soft fluid. empty fluid if not found
     */
    @Nonnull
    public static SoftFluid fromItem(Item filledContainerItem) {
        return ITEM_MAP.getOrDefault(filledContainerItem, EMPTY);
    }


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


    private static void register(SoftFluid s) {
        if (ModList.get().isLoaded(s.getRegistryName().getNamespace())) {
            for (Fluid f : s.getEquivalentFluids()) {
                //remove non-custom equivalent forge fluids in favor of this one
                if (FLUID_MAP.containsKey(f)) {
                    SoftFluid old = FLUID_MAP.get(f);
                    if (!old.isGenerated) {
                        ID_MAP.remove(old.getRegistryName());
                        old.getFilledContainer(Items.BUCKET).ifPresent(ITEM_MAP::remove);
                    }
                }
            }
            registerUnchecked(s);
        }
    }

    private static void registerUnchecked(SoftFluid... fluids) {
        Arrays.stream(fluids).forEach(s -> {
            s.getEquivalentFluids().forEach(f -> FLUID_MAP.put(f, s));
            s.getContainerList().getPossibleFilled().forEach(i -> ITEM_MAP.put(i, s));
            ResourceLocation key = s.getRegistryName();
            if (ID_MAP.containsKey(key)) {
                ID_MAP.put(key, SoftFluid.create(ID_MAP.get(key), s));
            } else {
                ID_MAP.put(key, s);
            }
        });
    }

    private static void convertAndRegisterAllForgeFluids() {
        for (Fluid f : ForgeRegistries.FLUIDS) {
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it meas that another equivalent fluid has already ben registered
                if (FLUID_MAP.containsKey(f)) continue;
                //is not equivalent: create new SoftFluid from forge fluid
                registerUnchecked((new SoftFluid.Builder(f)).build());
            } catch (Exception ignored) {
            }
        }
    }

    public static void acceptClientFluids(ClientBoundSyncFluidsPacket packet) {
        ID_MAP.clear();
        FLUID_MAP.clear();
        ITEM_MAP.clear();
        packet.getFluids().forEach(SoftFluidRegistry::register);
    }


    public static SoftFluidRegistry INSTANCE = new SoftFluidRegistry();

    public SoftFluidRegistry() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "soft_fluids");
    }

    private boolean initializedDispenser = false;

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ID_MAP.clear();
        FLUID_MAP.clear();
        ITEM_MAP.clear();

        for (var j : jsons.entrySet()) {
            Optional<SoftFluid> result = SoftFluid.CODEC.parse(JsonOps.INSTANCE, j.getValue())
                    .resultOrPartial(e -> Selene.LOGGER.error("Failed to read block growth JSON object for {} : {}", j.getKey(), e));
            result.ifPresent(SoftFluidRegistry::register);
        }
        convertAndRegisterAllForgeFluids();

        if (!initializedDispenser) {
            initializedDispenser = true;
            getRegisteredFluids().forEach(DispenserHelper::registerFluidBehavior);
        }
    }


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
        private boolean initialized;

        private FluidReference(ResourceLocation id) {
            this.value = null;
            this.id = id;
        }

        @Override
        public SoftFluid get() {
            synchronized (lock) {
                if (!initialized) {
                    initialized = true;
                    this.value = SoftFluidRegistry.get(id);
                }
                return value;
            }
        }
    }


}
