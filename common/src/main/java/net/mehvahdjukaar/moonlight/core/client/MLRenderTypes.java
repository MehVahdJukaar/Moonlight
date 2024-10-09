package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL13;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

public class MLRenderTypes extends RenderType {

    public static AtomicReference<ShaderInstance> TEXT_COLOR_SHADER = new AtomicReference<>();
    public static AtomicReference<ShaderInstance> PARTICLE_TRANSLUCENT_SHADER = new AtomicReference<>();


    public static final Function<ResourceLocation, RenderType> COLOR_TEXT = Util.memoize((p) ->
    {
        CompositeState compositeState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(TEXT_COLOR_SHADER::get))
                .setTextureState(new TextureStateShard(p,
                        false, true))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        return create("moonlight_text_color_mipped",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true,
                compositeState);
    });

    public static final Function<ResourceLocation, RenderType> TEXT_MIP = Util.memoize((p) ->
    {
        CompositeState compositeState = CompositeState.builder()
                .setShaderState(RENDERTYPE_TEXT_SHADER)
                .setTextureState(new TextureStateShard(p, false, true))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        return create("moonlight_text_mipped",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true,
                compositeState);
    });

    public static final Function<ResourceLocation, RenderType> ENTITY_SOLID_MIP = Util.memoize((resourceLocation) -> {
        CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, true))
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return create("moonlight_entity_solid_mipped", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256, true, false,
                compositeState);
    });

    public static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT_MIP = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, true))
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return RenderType.create("moonlight_entity_cutout_mipped",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                compositeState);
    });


    public static final ParticleRenderType PARTICLE_ADDITIVE_TRANSLUCENCY_RENDER_TYPE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator builder, TextureManager textureManager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.activeTexture(GL13.GL_TEXTURE2);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);
            //because of custom render type fuckery...

            RenderSystem.setShader(PARTICLE_TRANSLUCENT_SHADER::get);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            return builder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_ADDITIVE_TRANSLUCENT";
        }
    };


    public MLRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
}
