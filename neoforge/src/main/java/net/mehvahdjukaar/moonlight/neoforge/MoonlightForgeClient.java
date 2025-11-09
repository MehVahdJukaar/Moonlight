package net.mehvahdjukaar.moonlight.neoforge;


import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.nio.file.Path;

public class MoonlightForgeClient {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(MoonlightForgeClient::afterLoad);
        modEventBus.addListener(EventPriority.LOWEST, MoonlightForgeClient::onTextureStitch);
        NeoForge.EVENT_BUS.addListener(MoonlightForgeClient::onInputUpdate);
        NeoForge.EVENT_BUS.addListener( EventPriority.LOWEST, MoonlightForgeClient::itemTooltipEvent);
    }

    public static void itemTooltipEvent(ItemTooltipEvent event) {
        MoonlightClient.onItemTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
    }

    public static void afterLoad(FMLLoadCompleteEvent event) {
        for (var config : ModConfigHolder.getTrackedSpecs()) {
            if (!config.hasConfigScreen()) {
                ModList.get().getModContainerById(config.getModId()).ifPresent(c ->
                        c.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new));
            }
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