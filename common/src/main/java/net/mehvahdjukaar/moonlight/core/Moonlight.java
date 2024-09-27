package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.integration.CompatWoodTypes;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModNetworking;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.HashSet;
import java.util.Set;

import java.util.Collection;

@ApiStatus.Internal
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger("Moonlight");
    public static final boolean HAS_BEEN_INIT = true;
    public static final ThreadLocal<Boolean> CAN_EARLY_RELOAD_HACK = ThreadLocal.withInitial(() -> true);

    private static final Set<String> DEPENDENTS = new HashSet<>();


    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        //MoonlightEventsHelper.addListener( BlockSetInternal::addTranslations, AfterLanguageLoadEvent.class);
        CompatWoodTypes.init();
        MoonlightRegistry.init();

        ModNetworking.init();

        VillagerAIInternal.init();
        MapDataInternal.init();
        SoftFluidInternal.init();
        RegHelper.addDynamicDispenserBehaviorRegistration(Moonlight::registerBuiltinFluidBehavior);

        PlatHelper.addCommonSetup(Moonlight::commonSetup);
        PlatHelper.addReloadableCommonSetup(Moonlight::afterDataReload);

        PlatHelper.addServerReloadListener(new ItemListingManager(), Moonlight.res("villager_trade"));

        //hack
        BlockSetAPI.addDynamicRegistration((reg, wood) -> AdditionalItemPlacementsAPI.afterItemReg(),
                WoodType.class, BuiltInRegistries.BLOCK_ENTITY_TYPE);

        //client init
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.initClient();
        }
    }



    private static void commonSetup() {
        BlocksColorInternal.setup();

        if(PlatHelper.isDev()){
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }

    @EventCalled
    public static void onPlayerCloned(Player oldPlayer, Player newPlayer, boolean wasDeath) {
        if (wasDeath && !oldPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            var inv = oldPlayer.getInventory();
            int i = 0;
            for (var v : inv.items) {
                if (v != ItemStack.EMPTY) {
                    IDropItemOnDeathEvent e = IDropItemOnDeathEvent.create(v, oldPlayer, false);
                    MoonlightEventsHelper.postEvent(e, IDropItemOnDeathEvent.class);
                    if (e.isCanceled()) {
                        newPlayer.getInventory().setItem(i, e.getReturnItemStack());
                    }
                }
                i++;
            }
        }
    }

    @EventCalled
    private static void afterDataReload(RegistryAccess registryAccess, Boolean client) {
        RegistryAccessJsonReloadListener.runReloads(registryAccess);
        DynamicResourcePack.clearAfterReload(PackType.SERVER_DATA);
        DynamicHolder.clearCache();
    }

    @EventCalled
    public static void beforeServerStart() {
        SoftFluidInternal.doPostInitServer();
        SoftFluidStack.invalidateEmptyInstance();
    }

    public static void assertInitPhase() {
        if (!PlatHelper.isInitializing() && PlatHelper.getPlatform().isForge()) {
            throw new AssertionError("Method has to be called during main mod initialization phase. Client and Server initializer are not valid, you must call in the main one");
        }
    }

    private static void checkDatapackRegistry() {
        try {
            SoftFluidRegistry.getEmpty().value();
            MapDataRegistry.getDefaultType();
        } catch (Exception e) {
            throw new RuntimeException("""
                    Not all required entries were found in datapack registry. How did this happen?
                    This MUST be some OTHER mod messing up datapack registries (currently Cyanide is known to cause this).
                    Note that this could be caused by Paper or similar servers. Know that those are NOT meant to be used with mods""", e);
        }
    }

    @ApiStatus.Internal
    public static void addDependent(String modId) {
        if (!Set.of("minecraft", "neoforge", "fabric").contains(modId)) {
            DEPENDENTS.add(modId);
        }
    }

    public static Set<String> getDependents() {
        return Set.copyOf(DEPENDENTS);
    }

    public static void crashIfInDev() {
        if (PlatHelper.isDev()) throw new AssertionError();
    }

    public static void registerBuiltinFluidBehavior(DispenserHelper.Event event) {
        Set<Item> itemSet = new HashSet<>();
        for (SoftFluid f : SoftFluidRegistry.getRegistry(event.getRegistryAccess())) {
            Collection<FluidContainerList.Category> categories = f.getContainerList().getCategories();
            for (FluidContainerList.Category c : categories) {
                Item empty = c.getEmptyContainer();
                //prevents registering stuff twice
                if (empty != Items.AIR && !itemSet.contains(empty)) {
                    event.register(new DispenserHelper.FillFluidHolderBehavior(empty));
                    itemSet.add(empty);
                }
                for (Item full : c.getFilledItems()) {
                    if (full != Items.AIR && !itemSet.contains(full)) {
                        event.register(new DispenserHelper.FillFluidHolderBehavior(full));
                        itemSet.add(full);
                    }
                }
            }
        }
    }

}
