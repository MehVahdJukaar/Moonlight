package net.mehvahdjukaar.moonlight.neoforge;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.neoforge.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge.ModIngredientTypes;
import net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge.ResourceConditionsBridge;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.integration.neoforge.ModConfigSelectScreen;
import net.mehvahdjukaar.moonlight.core.misc.neoforge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        RegHelperImpl.runTasksOnInit();

        Moonlight.commonInit();
        NeoForge.EVENT_BUS.register(MoonlightForge.class);
        bus.addListener(MoonlightForge::registerCapabilities);
        ModLootModifiers.register();
        ModIngredientTypes.register();
        ResourceConditionsBridge.init();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightForgeClient.init(bus);

            if (PlatHelper.isModLoaded("configured")) {
                ModConfigSelectScreen.registerConfigScreen(MOD_ID, ModConfigSelectScreen::new);
            }
        }

        bus.addListener(MoonlightForge::addOldPoiEvent);


        PlatHelper.addCommonSetup(() -> {
            //stop bundling fabric api into shit!
            if (ModList.get().isLoaded("fabric_api")) {
                List<IModInfo> modsThatHaveFabric = new ArrayList<>();
                for (var modInfo : ModList.get().getMods()) {
                    var jij = modInfo.getOwningFile().getMods();
                    if (jij.stream().anyMatch(m -> m.getModId().equals("fabric_api"))) {
                        modsThatHaveFabric.add(modInfo);
                    }
                }
                Moonlight.LOGGER.error("Fabric API detected! This is not a Fabric mod, so please dont report related issues to MoonlightLib or its dependant. This can usually happen with connector or when having a mod that does NOT have a proper native Neoforge implementation as they SHOULD. This can easily lead to poor compatibility and issues. Proceed ar your own risk. \n Mods that bundled Fabric API: {}", modsThatHaveFabric);
            }
        });
    }


    @Deprecated(forRemoval = true)
    public static void addOldPoiEvent(ExtendPoiTypesEvent event) {
        for (var e : OLD_POI_EVENT.entrySet()) {
            var p = e.getKey();
            Iterable<? extends Block> blocks = e.getValue();
            for (var b : blocks) {
                event.addBlockToPoi(p, b);
            }
        }
    }

    //TODO: change or remove
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var e : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
            String modId = e.getKey().location().getNamespace();
            if (!Moonlight.isDependant(modId)) continue;
            try {
                var beType = e.getValue();
                var instance = beType.create(BlockPos.ZERO, beType.getValidBlocks().stream().findFirst().get().defaultBlockState());
                if (instance instanceof ItemDisplayTile) {
                    registerDefaultItemCap(event, beType);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void registerDefaultItemCap(RegisterCapabilitiesEvent event, BlockEntityType<?> beType) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, beType,
                (sidedContainer, side) -> side == null ? new InvWrapper((Container) sidedContainer) : new SidedInvWrapper((WorldlyContainer) sidedContainer, side));
    }

    @Nullable
    private static WeakReference<ICondition.IContext> context = null;

    @Nullable
    public static ICondition.IContext getConditionContext() {
        if (context == null) return null;
        return context.get();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        context = new WeakReference<>(event.getConditionContext());
    }

    @SubscribeEvent
    public static void beforeServerStart(ServerAboutToStartEvent event) {
        Moonlight.beforeServerStart(event.getServer().registryAccess());
    }

    @SubscribeEvent
    public static void onServerShuttingDown(ServerStoppingEvent event) {
        FakeLevelManager.invalidateAll();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FakeLevelManager.invalidateAll();
    }

    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            Moonlight.onDataSyncToPlayer(event.getPlayer(), true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                Moonlight.onDataSyncToPlayer(p, false);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                NetworkHelper.sendToClientPlayer(player, new ClientBoundSendLoginMessage());
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionUnload(LevelEvent.Unload event) {
        var level = event.getLevel();
        try {
            if (level.isClientSide()) {
                //got to be careful with classloading
                FPClientAccess.unloadLevel(level);
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Moonlight.onPlayerCloned(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    //this is shit

    //hack
    private static WeakReference<IEventBus> lastCurrentBus = null; //a bus. fallback. incase things go wrong works most of the time. mega ugly
    //turbo mega hack
    private static final ThreadLocal<WeakReference<IEventBus>> currentModBus = new ThreadLocal<>(); //ideally the bus of the mod we are constructing


    //mega hack
    public static IEventBus getCurrentBus() {
        var threadLocalBus = currentModBus.get();
        if (threadLocalBus != null && threadLocalBus.get() != null) {
            return threadLocalBus.get();
        }
        if (lastCurrentBus != null && lastCurrentBus.get() != null) {
            return lastCurrentBus.get();
        }
        throw new IllegalStateException("Bus is null. You must call RegHelper.startRegistering(IEventBus) before registering events");
    }

    /**
     * Call this before registering events
     */
    public static void startRegistering(IEventBus bus) {
        lastCurrentBus = new WeakReference<>(bus);
        currentModBus.set(lastCurrentBus);
    }


    private static final Map<ResourceKey<PoiType>, Iterable<? extends Block>> OLD_POI_EVENT = new ConcurrentHashMap<>();

    public static void addPoi(ResourceKey<PoiType> poi, Iterable<? extends Block> blocks) {
        OLD_POI_EVENT.put(poi, blocks);
    }
}

