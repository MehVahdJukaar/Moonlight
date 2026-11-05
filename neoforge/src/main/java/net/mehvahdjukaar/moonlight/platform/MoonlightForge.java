package net.mehvahdjukaar.moonlight.platform;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.platform.ForgeHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.platform.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.resources.recipe.platform.ModIngredientTypes;
import net.mehvahdjukaar.moonlight.api.resources.recipe.platform.ResourceConditionsBridge;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.moonlight.core.misc.platform.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
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
        RegHelperImpl.runTasksOnInit();

        Moonlight.commonInit();
        NeoForge.EVENT_BUS.register(MoonlightForge.class);
        bus.addListener(MoonlightForge::registerCapabilities);
        ModLootModifiers.register();
        ModIngredientTypes.register();
        ResourceConditionsBridge.init();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightForgeClient.init(bus);
        }

        bus.addListener(MoonlightForge::addOldPoiEvent);


        PlatHelper.addCommonSetup(() -> {
            //stop bundling fabric api into shit!
            var fabricApiFile = ModList.get().getModFileById("fabric_api");
            if (fabricApiFile != null) {
                //walk up the jar in jar chain
                List<String> bundleChain = new ArrayList<>();
                for (var f = fabricApiFile.getFile(); f != null; f = f.getDiscoveryAttributes().parent()) {
                    bundleChain.addFirst(f.getFileName());
                }
                String source = bundleChain.size() > 1 ? "bundled by " + bundleChain.getFirst() : "installed directly";
                Moonlight.LOGGER.warn("Fabric API detected ({})! This is not a Fabric mod, so please don't report related issues to MoonlightLib or its dependent(s). This can usually happen when using Connector, or when using a mod that does NOT have a native Neoforge implementation. This can easily lead to poor compatibility and other bizarre issues. Proceed at your own risk. \n Jar chain: {}", source, String.join(" -> ", bundleChain));
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
                    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, beType,
                            (sidedContainer, side) -> ForgeHelperImpl.makeDefaultInvHandler((Container) sidedContainer, side));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void registerDefaultItemCap(RegisterCapabilitiesEvent event, BlockEntityType<?> beType) {
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
        FakeLevelManager.invalidateAll(false);
        SidedInstance.clearAll();
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
            FakeGenericPlayer.unloadLevel(level);
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

    public static IEventBus getCurrentBus() {
        var currentBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        if (currentBus != null) return currentBus;

        //mega hack

        //not needed anymore
        var threadLocalBus = currentModBus.get();
        if (threadLocalBus != null && threadLocalBus.get() != null) {
            return threadLocalBus.get();
        }
        if (lastCurrentBus != null && lastCurrentBus.get() != null) {
            return lastCurrentBus.get();
        }
        throw new IllegalStateException("Bus is null. You must call RegHelper.startRegistering(IEventBus) before registering events");
    }

    //this is shit

    //hack
    private static WeakReference<IEventBus> lastCurrentBus = null; //a bus. fallback. incase things go wrong works most of the time. mega ugly
    //turbo mega hack
    private static final ThreadLocal<WeakReference<IEventBus>> currentModBus = new ThreadLocal<>(); //ideally the bus of the mod we are constructing


    /**
     * Call this before registering events
     */
    @Deprecated(forRemoval = true)
    public static void startRegistering(IEventBus bus) {
        lastCurrentBus = new WeakReference<>(bus);
        currentModBus.set(lastCurrentBus);
    }


    private static final Map<ResourceKey<PoiType>, Iterable<? extends Block>> OLD_POI_EVENT = new ConcurrentHashMap<>();

    public static void addPoi(ResourceKey<PoiType> poi, Iterable<? extends Block> blocks) {
        OLD_POI_EVENT.put(poi, blocks);
    }
}

