package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.integration.HardcodedBlockTypes;
import net.mehvahdjukaar.moonlight.api.misc.*;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.core.commands.BackCommand;
import net.mehvahdjukaar.moonlight.core.commands.ModCommands;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncWorldDataMessage;
import net.mehvahdjukaar.moonlight.core.network.ModNetworking;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.mehvahdjukaar.moonlight.core.set.DebugBlockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ApiStatus.Internal
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger("Moonlight");
    public static final boolean HAS_BEEN_INIT = true;

    public static final ThreadLocal<WeakReference<RegistryAccess>> EARLY_REGISTRY_ACCESS = new ThreadLocal<>();

    private static final Set<String> DEPENDENTS = new HashSet<>();
    private static boolean verboseLogging = false;

    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        CommonConfigs.init();

        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        //MoonlightEventsHelper.addListener( BlockSetInternal::addTranslations, AfterLanguageLoadEvent.class);
        HardcodedBlockTypes.init();
        MoonlightRegistry.init();
        ItemListingManager.init();

        ModNetworking.init();
        ModCommands.init();

        DynamicResourcesInternals.init();
        VillagerAIInternal.init();
        MapDataInternal.init();
        SoftFluidInternal.init();
        RegHelper.addDynamicDispenserBehaviorRegistration(Moonlight::registerBuiltinFluidBehavior);

        PlatHelper.addCommonSetup(Moonlight::commonSetup);
        PlatHelper.addReloadableCommonSetup(Moonlight::afterDataReloadOrDataSync);

        PlatHelper.addServerReloadListener(ItemListingManager::new, Moonlight.res("villager_trade"));

        addGlobalDatapackLoader();

        //client init
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.initClient();
        }

        //dumb. ensure stuff registered
        BlockSetAPI.addDynamicRegistration(Moonlight.MOD_ID, r -> {
        }, BuiltInRegistries.BLOCK);

        RegHelper.addItemsToTabsRegistration(event -> {
            if (event.getTab().hasAnyItems()) {
                event.addAfter(CreativeModeTabs.OP_BLOCKS,  i -> i.is(Items.JIGSAW), MoonlightRegistry.SPAWN_BOX_BLOCK.get());
            }
        });
    }

    private static void addGlobalDatapackLoader() {
        //global datapacks
        String globalPacksDir = CommonConfigs.GLOBAL_DATAPACKS_DIR.get();
        if (!globalPacksDir.isEmpty()) {
            Path path = PlatHelper.getGamePath().resolve(globalPacksDir);
            //create folder if not exists
            DirectoryValidator validator = new DirectoryValidator(a -> true);
            RegHelper.registerResourcePackSource(PackType.SERVER_DATA,
                    new FolderRepositorySource(path,
                            PackType.SERVER_DATA, PackSource.DEFAULT, validator));
            try {
                path.toFile().mkdirs();
            } catch (Exception ignored) {
            }
        }
    }

    private static void commonSetup() {

        BlocksColorInternal.INSTANCE.setup();
        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightClient.setupClient();
        }

        if (CommonConfigs.EXTRA_DEBUG.get() || PlatHelper.isDev()) {
            DebugBlockTypes.writeToFile();
        }
    }

    @EventCalled
    public static void onPlayerCloned(Player oldPlayer, Player newPlayer, boolean wasDeath) {
        BlockPos oldPos = oldPlayer.blockPosition();
        var oldDim = oldPlayer.level().dimension();
        BackCommand.onTeleported(newPlayer, oldPos, oldDim);
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
        Level world = player.level();
        for (var type : MoonlightRegistry.WORLD_SAVED_DATA_TYPE_REGISTRY) {
            WorldSavedData data = type.getData(world);
            if (data.getType().isSyncable()) {
                NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncWorldDataMessage<>(data));
            }
        }
    }

    //by mixin, even earlier, needed for recipe manager. not needed in 1.21.1+
    public static void setServerRegistryAccess(RegistryAccess.Frozen registryAccess) {
        EARLY_REGISTRY_ACCESS.set(new WeakReference<>(registryAccess));
    }

    @EventCalled
    private static void afterDataReloadOrDataSync(RegistryAccess registryAccess, boolean client) {
        EARLY_REGISTRY_ACCESS.set(new WeakReference<>(registryAccess));
        RegistryAccessJsonReloadListener.runReloads(registryAccess);
        DynamicResourcesInternals.clearAfterReload(PackType.SERVER_DATA);
        DynamicHolder.clearCache();

        HolderRef.clearCache();
        DispenserHelper.reload(registryAccess, client);
    }

    @EventCalled
    public static void beforeServerStart(RegistryAccess ra) {
        SoftFluidInternal.doPostInitServer(ra);
    }

    public static void assertInitPhase() {
        if (!PlatHelper.isInitializing() && PlatHelper.getPlatform().isForge()) {
            throw new AssertionError("Method has to be called during main mod initialization phase. Client and Server initializer are not valid, you must call in the main one");
        }
    }

    public static boolean isInitPhase() {
        return PlatHelper.isInitializing() || !PlatHelper.getPlatform().isForge();
    }

    public static void assertAfterInitPhase() {
        if (PlatHelper.isInitializing()) {
            throw new AssertionError("Method has to be called after main mod initialization phase. Client and Server initializer are not valid, you must call in the main one");
        }
    }

    @ApiStatus.Internal
    public static void addDependent(String modId) {
        if (!Set.of("minecraft", "neoforge", "fabric").contains(modId)) {
            DEPENDENTS.add(modId);
        }
    }

    //not ideal but works most of the times. this is populated when a mod invokes a ML registry function
    public static Set<String> getDependents() {
        return Set.copyOf(DEPENDENTS);
    }

    public static boolean isDependant(String modId) {
        return DEPENDENTS.contains(modId);
    }

    public static void crashIfInDev(String message) {
        if (PlatHelper.isDev()) throw new RuntimeException(message);
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
        for (SoftFluid f : SoftFluidRegistry.get(event.getRegistryAccess())) {
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

    public static void setVerboseLogging(boolean b) {
        verboseLogging = b;
    }

    public static boolean isVerboseLogging() {
        return verboseLogging;
    }
}
