package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;

public class MLRenderTypes extends RenderType {

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

    private static class TestStateShard extends TextureStateShard {
        private TestStateShard(ResourceLocation resLoc) {
            super(resLoc, false, true);
            this.setupState = () -> {
                TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                var t = texturemanager.getTexture(resLoc);
                t.setFilter(false, true);
                GlStateManager._texParameter(3553, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                RenderSystem.setShaderTexture(0, resLoc);
            };
        }
    }


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

    public MLRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
}
