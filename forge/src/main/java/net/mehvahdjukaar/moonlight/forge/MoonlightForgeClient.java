package net.mehvahdjukaar.moonlight.forge;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
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
        modEventBus.addListener(MoonlightForgeClient::registerShader);
        modEventBus.addListener(EventPriority.LOWEST, MoonlightForgeClient::onTextureStitch);
    }

    private static ShaderInstance translucentParticle;
    public static ShaderInstance textAlphaShader;

    public static ShaderInstance getTranslucentParticle() {
        return translucentParticle;
    }

    public static ShaderInstance getTextAlphaShader() {
        return textAlphaShader;
    }

    public static void registerShader(RegisterShadersEvent event) {
        try {
            ShaderInstance translucentParticleShader = new ShaderInstance(event.getResourceProvider(),
                    Moonlight.res("particle_translucent"), DefaultVertexFormat.POSITION_TEX);

            event.registerShader(translucentParticleShader, s -> translucentParticle = s);

            ParticleUtil.particleShader = MoonlightForgeClient::getTranslucentParticle;
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to parse shader: " + e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(),
                    Moonlight.res("text_alpha_color"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            event.registerShader(shader, s -> textAlphaShader = s);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to parse shader: " + e);
        }
    }


    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {

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