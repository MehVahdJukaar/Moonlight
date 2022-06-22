package net.mehvahdjukaar.selene.fluids;

import net.mehvahdjukaar.selene.Moonlight;
import net.mehvahdjukaar.selene.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class SoftFluidRegistry {

    public static final SoftFluid EMPTY = new SoftFluid.Builder(Fluids.EMPTY).build();

    public static final ResourceKey<Registry<SoftFluid>> KEY = ResourceKey.createRegistryKey(Moonlight.res("soft_fluids"));
    public static final DeferredRegister<SoftFluid> DEFERRED_REGISTER = DeferredRegister.create(KEY, KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<SoftFluid>> SOFT_FLUIDS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<SoftFluid>()
                    .setDefaultKey(EMPTY.getRegistryName())
                    .dataPackRegistry(SoftFluid.CODEC, SoftFluid.CODEC)
                    .onAdd(SoftFluidRegistry::onAdd)
                    .onCreate(SoftFluidRegistry::onCreate)
                    .onClear(SoftFluidRegistry::onClear)
                    .onValidate(SoftFluidRegistry::addExistingForgeFluids)
                    .allowModification()
                    .disableSaving());

    //slave maps
    // containers -> SoftFluid
    private static final HashMap<Item, SoftFluid> ITEM_MAP = new HashMap<>();
    // forge fluid  -> SoftFluid
    private static final HashMap<Fluid, SoftFluid> FLUID_MAP = new HashMap<>();


    public static Collection<SoftFluid> getValues() {
        return SOFT_FLUIDS.get().getValues();
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
        return SOFT_FLUIDS.get().getValue(id);
    }

    public static Optional<SoftFluid> getOptional(ResourceLocation id) {
        var s = get(id);
        if (s.isEmpty()) return Optional.empty();
        return Optional.of(s);
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

    public static void onAdd(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage, int id,
                             ResourceKey<SoftFluid> key, SoftFluid s, @Nullable SoftFluid oldObj) {

        s.getEquivalentFluids().forEach(f -> FLUID_MAP.put(f, s));
        s.getContainerList().getPossibleFilled().forEach(i -> {
            //dont associate water to potion bottle
            if (i != Items.POTION || !s.getRegistryName().toString().equals("minecraft:water")) {
                ITEM_MAP.put(i, s);
            }
        });
        //merge stuff
        if (oldObj != null) {
            owner.register(key.location(), SoftFluid.merge(oldObj, s));
        }
    }

    public static void onCreate(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        ITEM_MAP.clear();
        FLUID_MAP.clear();
        owner.register(EMPTY.getRegistryName(), EMPTY);
    }

    public static void onClear(IForgeRegistryInternal<SoftFluid> owner, RegistryManager stage) {
        ITEM_MAP.clear();
        FLUID_MAP.clear();
    }

    private static void addExistingForgeFluids(IForgeRegistryInternal<SoftFluid> softFluids, RegistryManager registryManager, int i, ResourceLocation resourceLocation, SoftFluid softFluid) {
        //only runs on the first object
        if (i == 0) {
            for (Fluid f : ForgeRegistries.FLUIDS) {
                try {
                    if (f == null) continue;
                    if (f instanceof FlowingFluid flowingFluid && flowingFluid.getSource() != f) continue;
                    if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                    //if fluid map contains fluid it means that another equivalent fluid has already been registered
                    if (FLUID_MAP.containsKey(f)) continue;
                    //is not equivalent: create new SoftFluid from forge fluid
                    if (Utils.getID(f) != null) {
                        SoftFluid sf = (new SoftFluid.Builder(f)).build();
                        softFluids.register(sf.getRegistryName(), sf);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}

