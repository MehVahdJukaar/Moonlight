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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
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
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
            // config screen wiring lives in MoonlightForgeClient#afterLoad (gated by the custom_config_screen client
            // config); the old Configured delegation is kept in the integration classes but no longer registered here
        }

        PlatHelper.addCommonSetup(() -> {
            //stop bundling fabric api into shit!
            if (ModList.get().isLoaded("fabric_api")) {
                Set<String> modsThatHaveFabric = new HashSet<>();
                for (var modInfo : ModList.get().getMods()) {
                    var jij = modInfo.getOwningFile().getMods();
                    if (jij.stream().anyMatch(m -> m.getModId().equals("fabric_api"))) {
                        modsThatHaveFabric.add(modInfo.getOwningFile().getFile().getFileName());
                    }
                }
                Moonlight.LOGGER.error("Fabric API detected! This is not a Fabric mod, so please don't report related issues to MoonlightLib or its dependent(s). This can usually happen when using Connector, or when using a mod that does NOT have a native Neoforge implementation. This can easily lead to poor compatibility and other bizarre issues. Proceed at your own risk. \n Mods that bundle Fabric API: {}", modsThatHaveFabric);
            }
        });
    }


    //TODO: change or remove
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var e : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
            String modId = e.getKey().identifier().getNamespace();
            if (!Moonlight.isDependant(modId)) continue;
            try {
                var beType = e.getValue();
                var instance = beType.create(BlockPos.ZERO, beType.getValidBlocks().stream().findFirst().get().defaultBlockState());
                if (instance instanceof ItemDisplayTile) {
                    event.registerBlockEntity(Capabilities.Item.BLOCK, beType,
                            (sidedContainer, side) -> ForgeHelperImpl.makeDefaultInvHandler((Container) sidedContainer, side));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    private static WeakReference<ICondition.IContext> context = null;

    @Nullable
    public static ICondition.IContext getConditionContext() {
        if (context == null) return null;
        return context.get();
    }

    @SubscribeEvent
    public static void onResourceReload(AddServerReloadListenersEvent event) {
        context = new WeakReference<>(event.getConditionContext());
    }

    @SubscribeEvent
    public static void beforeServerStart(ServerAboutToStartEvent event) {
        Moonlight.beforeServerStart(event.getServer().registryAccess());
    }

    @SubscribeEvent
    public static void onServerShuttingDown(ServerStoppingEvent event) {
        FakeLevelManager.invalidateAll();
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

}

