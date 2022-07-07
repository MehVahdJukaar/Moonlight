package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

//TODO: maybe split into api/core?
public class SoftFluidRegistry {


    public static final SoftFluid EMPTY = new SoftFluid.Builder(new ResourceLocation(""),
            new ResourceLocation("")).build();

    public static final ResourceKey<Registry<SoftFluid>> REGISTRY_KEY = ResourceKey.createRegistryKey(Moonlight.res("soft_fluids"));


    @ExpectPlatform
    public static Map<Fluid, SoftFluid> getFluidsMap() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<Item, SoftFluid> getItemsMap() {
        throw new AssertionError();
    }


    //hackyyy
    public static Registry<SoftFluid> getDataPackRegistry() {
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
        if (id.getNamespace().equals("selene")) id = Moonlight.res(id.getPath()); //backwards compat
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


    private static void populateSlaveMaps() {
        var itemMap = getItemsMap();
        var fluidsMap = getFluidsMap();
        for (var s : getValues()) {
            if (PlatformHelper.isModLoaded(s.getFromMod())) {
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

    //TODO: call these
    public static void postInitClient() {
        populateSlaveMaps();
        SoftFluidClient.refresh();
    }

    public static void postInitServer() {
        populateSlaveMaps();
        addExistingForgeFluids();
    }

    @ExpectPlatform
    private static void addExistingForgeFluids() {
        throw new AssertionError();
    }
}

