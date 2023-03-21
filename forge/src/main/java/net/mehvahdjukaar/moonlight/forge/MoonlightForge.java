package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.forge.MapDecorationRegistryImpl;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeServerPlayer;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
        MinecraftForge.EVENT_BUS.register(this);
        ModLootModifiers.register();
        ModLootConditions.register();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (PlatHelper.getPhysicalSide().isClient()) {
            modEventBus.addListener(MoonlightForgeClient::registerShader);
        }
        modEventBus.addListener(SoftFluidRegistryImpl::registerDataPackRegistry);
        modEventBus.addListener(MapDecorationRegistryImpl::registerDataPackRegistry);
    }

    //hacky but eh
    @SubscribeEvent
    public void onTagUpdated(TagsUpdatedEvent event) {
        RegistryAccessJsonReloadListener.runReloads(event.getRegistryAccess());
    }

    @Nullable
    private static WeakReference<ICondition.IContext> context = null;

    @Nullable
    public static ICondition.IContext getConditionContext() {
        if (context == null) return null;
        return context.get();
    }

    @SubscribeEvent
    public void onResourceReload(AddReloadListenerEvent event) {
        context = new WeakReference<>(event.getConditionContext());
    }

    @SubscribeEvent
    public void onDataSync(OnDatapackSyncEvent event) {
        SoftFluidRegistry.onDataLoad();
        //send syncing packets
        if (event.getPlayer() != null) {
            SoftFluidRegistry.onDataSyncToPlayer(event.getPlayer(), true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                SoftFluidRegistry.onDataSyncToPlayer(p, true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                ModMessages.CHANNEL.sendToClientPlayer(player,
                        new ClientBoundSendLoginPacket());
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDimensionUnload(LevelEvent.Unload event) {
        var level = event.getLevel();
        try {
            if (level instanceof ServerLevel sl) {
                FakeServerPlayer.unloadLevel(sl);
            } else if (level.isClientSide()) {
                //got to be careful with classloading
                FPClientAccess.unloadLevel(level);
            }
        } catch (Exception ignored) {
        }
    }

}

