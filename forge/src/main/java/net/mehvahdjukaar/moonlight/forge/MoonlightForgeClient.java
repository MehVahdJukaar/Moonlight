package net.mehvahdjukaar.moonlight.forge;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MoonlightForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoonlightForgeClient {

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
    /*
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RenderedTexturesManager.updateTextures();
        }
    }*/

    private static ShaderInstance translucentParticle;

    public static ShaderInstance getTranslucentParticle() {
        return translucentParticle;
    }

    public static void registerShader(RegisterShadersEvent event) {
        try {
            ShaderInstance translucentParticleShader = new ShaderInstance(event.getResourceManager(),
                    Moonlight.res("particle_translucent"), DefaultVertexFormat.POSITION_TEX);

            event.registerShader(translucentParticleShader, s -> translucentParticle = s);

            ParticleUtil.particleShader = MoonlightForgeClient::getTranslucentParticle;
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to parse shader: " + e);
        }
    }


    public static void onTextureStitch(TextureStitchEvent.Post event) {
        for (var p : DynamicResourcePack.INSTANCES) {
            if (p instanceof DynamicTexturePack) {
                p.clearResources();
            }
        }
    }

}
