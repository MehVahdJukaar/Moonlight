package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.forge.ConfigSpecWrapper;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.misc.FakeLevelManager;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge() {
        Moonlight.commonInit();
        MinecraftForge.EVENT_BUS.register(MoonlightForge.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MoonlightForge::configsLoaded);
        ModLootModifiers.register();
        ModLootConditions.register();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MoonlightForgeClient.init();
        }
    }


    public static void configsLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ((ConfigSpecWrapper) CommonConfigs.CONFIG).getSpec()) {
            if (!ModLoader.get().hasCompletedState("LOAD_REGISTRIES")) {
                throw new IllegalStateException("Some OTHER mod has forcefully loaded ALL other mods configs before the registry phase. This should not be done. Dont report this to Moonlight. Refusing to proceed further");
            }
        }
    }

    //hacky but eh
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTagUpdated(TagsUpdatedEvent event) {
        Moonlight.afterDataReload(event.getRegistryAccess(), event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
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
                ModMessages.CHANNEL.sendToClientPlayer(player,
                        new ClientBoundSendLoginPacket());
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLevelUnload(LevelEvent.Unload event) {
        var level = event.getLevel();
        try {
            if (PlatHelper.getPhysicalSide().isClient()) {
                //got to be careful with classloading
                FPClientAccess.unloadLevel(level);
            }
            FakeGenericPlayer.unloadLevel(level);
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Moonlight.onPlayerCloned(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onLevelLoaded(LevelEvent.Unload event) {
        DynamicHolder.clearCache();
    }

}

