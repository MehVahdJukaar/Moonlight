package net.mehvahdjukaar.moonlight.platform;


import net.mehvahdjukaar.moonlight.api.client.config.MoonlightConfigSelectScreen;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.DynamicTextureRenderer;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashSet;
import java.util.Set;

public class MoonlightForgeClient {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(MoonlightForgeClient::afterLoad);
        modEventBus.addListener(EventPriority.LOWEST, MoonlightForgeClient::onTextureStitch);
        NeoForge.EVENT_BUS.addListener(MoonlightForgeClient::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(MoonlightForgeClient::onInputUpdate);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MoonlightForgeClient::itemTooltipEvent);
    }

    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FakeLevelManager.invalidateAll();
        DynamicTextureRenderer.clearCache();
        RenderedTexturesManager.clearCache();
    }

    public static void itemTooltipEvent(ItemTooltipEvent event) {
        MoonlightClient.onItemTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
    }

    // mods that opt out of the native Moonlight screen and keep the old (vanilla) config screen
    private static final Set<String> LEGACY_CONFIG_SCREEN_MODS = Set.of("supplementaries");

    public static void afterLoad(FMLLoadCompleteEvent event) {
        if (CompatHandler.CONFIGURED) return;
        // TEST WIRING: route each mod's config button to the native Moonlight screen (one extension point
        // per mod container; the vanilla ConfigurationScreen is kept as a fallback).
        Set<String> registered = new HashSet<>();
        for (var config : ModConfigHolder.getTrackedSpecs()) {
            String modId = config.getModId();
            if (!registered.add(modId)) continue;
            boolean legacy = LEGACY_CONFIG_SCREEN_MODS.contains(modId);
            ModList.get().getModContainerById(modId).ifPresent(c ->
                    c.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> {
                        // list every config registered for this mod (common, client, ...); opens the single one directly
                        var screen = legacy ? null : MoonlightConfigSelectScreen.create(modId, parent, null);
                        return screen != null ? screen : new ConfigurationScreen(container, parent);
                    }));
        }
    }

    public static void onTextureStitch(TextureAtlasStitchedEvent event) {
        MoonlightClient.afterTextureReload();
    }

    public static void onInputUpdate(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Entity riddenEntity = mc.player.getVehicle();
            if (riddenEntity instanceof IControllableVehicle listener) {
                Input movementInput = event.getInput();
                listener.onInputUpdate(movementInput.left, movementInput.right,
                        movementInput.up, movementInput.down,
                        mc.options.keySprint.isDown(), movementInput.jumping);
            }
        }
    }


}