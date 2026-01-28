package net.mehvahdjukaar.moonlight.forge;


import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.misc.FakeLevelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MoonlightForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoonlightForgeClient {

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(EventPriority.LOWEST, MoonlightForgeClient::onTextureStitch);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FakeLevelManager.invalidateAll();
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTextureStitch(TextureStitchEvent.Post event) {
        MoonlightClient.afterTextureReload();
    }

}