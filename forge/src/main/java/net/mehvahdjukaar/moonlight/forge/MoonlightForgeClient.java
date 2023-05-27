package net.mehvahdjukaar.moonlight.forge;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

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

    private static ShaderInstance translucentParticle;

    public static ShaderInstance getTranslucentParticle() {
        return translucentParticle;
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
    }

   static int i = 0;

    @SubscribeEvent
    public static void onRenderWorld(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            if(Minecraft.getInstance().level != null && (++i)%40==0) {
                var t = RenderedTexturesManager.requestFlatItemStackTexture(new ResourceLocation("aa"),
                        Items.PISTON.getDefaultInstance(), 32);
                Path p = Paths.get("debug");
                try {
                    Screenshot.grab(Minecraft.getInstance().gameDirectory, "test.png",
                           t.getFrameBuffer(), arg -> {});
                    t.saveTextureToFile(p);

                } catch (Exception e) {
                }
            }
        }
    }



}
