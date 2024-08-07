package net.mehvahdjukaar.moonlight.neoforge;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.neoforge.ForgeConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge.ResourceConditionsBridge;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.moonlight.core.misc.neoforge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.neoforge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;
    public static final ModConfigSpec SPEC = ((ForgeConfigHolder) ConfigBuilder.create(MOD_ID, ConfigType.COMMON)
            .build()).getSpec();

    public MoonlightForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);

        Moonlight.commonInit();
        NeoForge.EVENT_BUS.register(MoonlightForge.class);
        bus.addListener(MoonlightForge::configsLoaded);
        bus.addListener(MoonlightForge::registerCapabilities);
        ModLootModifiers.register();
        ModLootConditions.register();
        ResourceConditionsBridge.init();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightForgeClient.init(bus);
            MoonlightClient.initClient();
        }
    }


    public static void configsLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            //if (!ModLoader.hasCompletedState("LOAD_REGISTRIES")) {
            //    throw new IllegalStateException("Some OTHER mod has forcefully loaded ALL other mods configs before the registry phase. This should not be done. Dont report this to Moonlight. Refusing to proceed further");
            //}
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> beType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            var instance = beType.create(BlockPos.ZERO, beType.getValidBlocks().stream().findFirst().get().defaultBlockState());
            if (instance instanceof ItemDisplayTile) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, beType,
                        (sidedContainer, side) -> side == null ? new InvWrapper((Container) sidedContainer) : new SidedInvWrapper((WorldlyContainer) sidedContainer, side));
            }
        }
    }


    //hacky but eh
    @SubscribeEvent
    public static void onTagUpdated(TagsUpdatedEvent event) {
        Moonlight.afterDataReload(event.getRegistryAccess());
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
        Moonlight.beforeServerStart();
    }

    @SubscribeEvent
    public static void beforeServerStart(ServerStoppedEvent event) {
        DummyWorld.clearInstance();
    }

    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        //send syncing packets just on login
        if (event.getPlayer() != null) {
            SoftFluidInternal.onDataSyncToPlayer(event.getPlayer(), true);
        }//else joined = false
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                NetworkHelper.sendToClientPlayer(player, new ClientBoundSendLoginPacket());
            } catch (Exception ignored) {
            }
        } else Moonlight.checkDatapackRegistry();
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

    @SubscribeEvent
    public static void onLevelLoaded(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) Moonlight.checkDatapackRegistry();
    }

    private static WeakReference<IEventBus> currentBus = null;

    public static IEventBus getCurrentBus() {
        var b = currentBus.get();
        if (b == null)
            throw new IllegalStateException("Bus is null. You must call RegHelper.startRegistering(IEventBus) before registering events");
        return b;
    }

    /**
     * Call this before registering events
     */
    public static void startRegistering(IEventBus bus) {
        currentBus = new WeakReference<>(bus);
    }

}

