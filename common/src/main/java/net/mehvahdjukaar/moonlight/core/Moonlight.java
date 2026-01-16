package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.integration.HardcodedBlockTypes;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynResourceGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.mehvahdjukaar.moonlight.core.set.DebugBlockTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@ApiStatus.Internal
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger("Moonlight");
    public static final boolean HAS_BEEN_INIT = true;

    public static final ThreadLocal<WeakReference<RegistryAccess>> EARLY_REGISTRY_ACCESS = new ThreadLocal<>();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        CommonConfigs.init();

        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        //MoonlightEventsHelper.addListener( BlockSetInternal::addTranslations, AfterLanguageLoadEvent.class);
        HardcodedBlockTypes.init();
        MoonlightRegistry.init();

        ModMessages.init();

        BlockTypeSwapIngredient.init();
        VillagerAIInternal.init();
        ItemListingRegistry.init();
        MapDataInternal.init();
        SoftFluidInternal.init();
        RegHelper.addDynamicDispenserBehaviorRegistration(Moonlight::registerBuiltinFluidBehavior);

        PlatHelper.addCommonSetup(Moonlight::commonSetup);

        PlatHelper.addServerReloadListener(new ItemListingRegistry(), Moonlight.res("villager_trades"));

        //hack
        BlockSetAPI.addDynamicRegistration((reg, wood) -> AdditionalItemPlacementsAPI.afterItemReg(),
                WoodType.class, BuiltInRegistries.BLOCK_ENTITY_TYPE);

        addGlobalDatapackLoader();
        PlatHelper.addServerReloadListener(BlocksColorInternal.INSTANCE,
                Moonlight.res("blocks_color_data"));


        //client init
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.initClient();
        }

        if (PlatHelper.isDev()) {
            new MlTestGen().register();
        }
    }

    private static class MlTestGen extends DynServerResourcesGenerator {
        public MlTestGen() {
            super(new DynamicDataPack(Moonlight.res("generated_pack")));
            this.dynamicPack.addNamespaces("minecraft");
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        @Override
        public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            super.regenerateDynamicAssets(executor);
            executor.accept((a, b) -> {
                SimpleTagBuilder st = SimpleTagBuilder.of(Moonlight.res("test_tag"));
                st.addEntry(Blocks.DIAMOND_BLOCK);
                st.addEntry(Blocks.DIAMOND_ORE);
                b.addTag(st, Registries.BLOCK);
            });
        }
    }

    private static void addGlobalDatapackLoader() {
        //global datapacks
        String globalPacksDir = CommonConfigs.GLOBAL_DATAPACKS_DIR.get();
        if (!globalPacksDir.isEmpty()) {
            Path path = PlatHelper.getGamePath().resolve(globalPacksDir);
            //create folder if not exists
            RegHelper.registerResourcePackSource(PackType.SERVER_DATA,
                    new FolderRepositorySource(path,
                            PackType.SERVER_DATA, PackSource.DEFAULT));
            try {
                path.toFile().mkdirs();
            } catch (Exception ignored) {
            }
        }
    }

    private static void commonSetup() {
        BlocksColorInternal.INSTANCE.setup();

        if (PlatHelper.isDev()) {
            //MixinEnvironment.getCurrentEnvironment().audit();
        }
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.setupClient();
        }

        if (CommonConfigs.EXTRA_DEBUG.get()) {
            DebugBlockTypes.writeToFile();
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
    public static void onDataSyncToPlayer(ServerPlayer player, boolean joined) {
        //send syncing packets just on login. datapack registries don't change on reload
        if (joined) {
            SoftFluidInternal.onDataSyncToPlayer(player, true);
        }
    }

    //by mixin, even earlier, needed for recipe manager. not needed in 1.21.1+
    public static void setServerRegistryAccess(RegistryAccess.Frozen registryAccess) {
        EARLY_REGISTRY_ACCESS.set(new WeakReference<>(registryAccess));
    }

    @EventCalled
    public static void afterDataReload(RegistryAccess registryAccess, boolean client) {
        EARLY_REGISTRY_ACCESS.set(new WeakReference<>(registryAccess));

        RegistryAccessJsonReloadListener.runReloads(registryAccess);
        DynResourceGenerator.clearAfterReload(PackType.SERVER_DATA);
        DynamicHolder.clearCache();
        DispenserHelper.reload(registryAccess);
    }

    @EventCalled
    public static void beforeServerStart(RegistryAccess ra) {
        SoftFluidInternal.doPostInitServer(ra);
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

    public static void checkDataPackRegistry() {
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

    public static void crashIfInDev(String message) {
        if (PlatHelper.isDev()) throw new AssertionError(message);
        else {
            Moonlight.LOGGER.error(message);
        }
    }

    public static void crashIfInDev() {
        crashIfInDev("");
    }

    public static void logIfInDev(String s) {
        if (PlatHelper.isDev()) LOGGER.error(s);
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

    private static boolean warnedInvalidServer = false;

    public static void warnInvalidServer() {
        if (!warnedInvalidServer) {
            LOGGER.error("It seems like you are on a VANILLA server. This could cause issues and is NOT supported and you are OUT OF SUPPORT!");
            warnedInvalidServer = true;
        }

    }
}
