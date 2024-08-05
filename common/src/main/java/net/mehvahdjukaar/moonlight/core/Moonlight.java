package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.integration.CompatWoodTypes;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.MixinEnvironment;

@ApiStatus.Internal
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger("Moonlight");
    public static final boolean HAS_BEEN_INIT = true;
    public static final ThreadLocal<Boolean> CAN_EARLY_RELOAD_HACK = ThreadLocal.withInitial(() -> true);

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        //MoonlightEventsHelper.addListener( BlockSetInternal::addTranslations, AfterLanguageLoadEvent.class);
        CompatWoodTypes.init();
        MoonlightRegistry.init();

        ModMessages.init();

        VillagerAIInternal.init();
        MapDataInternal.init();
        SoftFluidInternal.init();

        PlatHelper.addCommonSetup(Moonlight::commonSetup);

        PlatHelper.addServerReloadListener(new ItemListingRegistry(), Moonlight.res("villager_trades"));

        //hack
        BlockSetAPI.addDynamicRegistration((reg, wood) -> AdditionalItemPlacementsAPI.afterItemReg(),
                WoodType.class, BuiltInRegistries.BLOCK_ENTITY_TYPE);

        //client init
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.initClient();
        }

        if(PlatHelper.isDev()){
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }

    private static void commonSetup() {
        BlocksColorInternal.setup();
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
    public static void afterDataReload(RegistryAccess registryAccess) {
        RegistryAccessJsonReloadListener.runReloads(registryAccess);
        DynamicResourcePack.clearAfterReload(PackType.SERVER_DATA);
        DataObjectReference.onDataReload();
    }

    @EventCalled
    public static void beforeServerStart() {
        SoftFluidInternal.doPostInitServer();
        SoftFluidStack.invalidateEmptyInstance();
    }

    public static void assertInitPhase() {
        if (!PlatHelper.isInitializing()) {
            //TODO: re add once all mods are updated. we have to let fabric use its own initializer. this is too strict so we ignore
            if (PlatHelper.isDev() && PlatHelper.getPlatform().isForge()) {
                throw new AssertionError("Method has to be called during main mod initialization phase. Client and Server initializer are not valid, you must call in the main one");
            }
        }
    }

    public static MapItemSavedData getMapDataFromKnownKeys(ServerLevel level, int mapId) {
        var d = level.getMapData(MapItem.makeKey(mapId));
        if (d == null) {
            d = level.getMapData("magicmap_" + mapId);
            if (d == null) {
                d = level.getMapData("mazemap_" + mapId);
            }
        }
        return d;
    }

    public static void checkDatapackRegistry() {
        try {
            SoftFluidRegistry.getEmpty();
            MapDataRegistry.getDefaultType();
        } catch (Exception e) {
            throw new RuntimeException("""
                    Not all required entries were found in datapack registry. How did this happen?
                    This MUST be some OTHER mod messing up datapack registries (currently Cyanide is known to cause this).
                    Note that this could be caused by Paper or similar servers. Know that those are NOT meant to be used with mods""", e);
        }
    }

}
