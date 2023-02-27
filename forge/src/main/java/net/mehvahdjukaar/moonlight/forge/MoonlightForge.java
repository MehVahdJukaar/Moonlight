package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.forge.ConfigBuilderImpl;
import net.mehvahdjukaar.moonlight.api.util.fake_player.FakePlayerManager;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
    }
    //hacky but eh
    @SubscribeEvent
    public static void onTagUpdated(TagsUpdatedEvent event) {
        RegistryAccessJsonReloadListener.runReloads(event.getRegistryAccess());
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
        FakePlayerManager.unloadLevel(event.getLevel());
    }

}

