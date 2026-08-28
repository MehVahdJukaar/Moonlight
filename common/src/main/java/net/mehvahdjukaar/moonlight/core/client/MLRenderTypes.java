package net.mehvahdjukaar.moonlight.core.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.particle.SingleQuadParticle;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Supplier;

public class MLRenderTypes {

    // vanilla's atlas sampler never mipmaps
    private static final Supplier<GpuSampler> MIPMAP_SAMPLER = Suppliers.memoize(() -> RenderSystem.getDevice()
            .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                    FilterMode.LINEAR, FilterMode.NEAREST, 1, OptionalDouble.empty()));

    // paints the texture's shape with the vertex color. Uses the text vertex shader, so POSITION_COLOR_TEX_LIGHTMAP quads
    public static final RenderPipeline TEXT_ALPHA_COLOR = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withLocation(Moonlight.res("pipeline/text_alpha_color"))
            .withVertexShader("core/rendertype_text")
            .withFragmentShader(Moonlight.res("core/text_alpha_color"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .build();

    // vanilla particle shaders with additive blending
    public static final RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Moonlight.res("pipeline/additive_particle"))
            .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)))
            .build();

    public static final SingleQuadParticle.Layer ADDITIVE_TERRAIN_PARTICLE_LAYER =
            new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_BLOCKS, ADDITIVE_PARTICLE);

    public static final Function<Identifier, RenderType> COLOR_TEXT = Util.memoize(texture ->
            RenderType.create("moonlight_text_color", RenderSetup.builder(TEXT_ALPHA_COLOR)
                    .withTexture("Sampler0", texture, MIPMAP_SAMPLER)
                    .useLightmap()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> TEXT_MIP = Util.memoize(texture ->
            RenderType.create("moonlight_text_mipped", RenderSetup.builder(RenderPipelines.TEXT)
                    .withTexture("Sampler0", texture, MIPMAP_SAMPLER)
                    .useLightmap()
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> ENTITY_SOLID_MIP = Util.memoize(texture ->
            RenderType.create("moonlight_entity_solid_mipped", RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                    .withTexture("Sampler0", texture, MIPMAP_SAMPLER)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup()));

    public static final Function<Identifier, RenderType> ENTITY_CUTOUT_MIP = Util.memoize(texture ->
            RenderType.create("moonlight_entity_cutout_mipped", RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
                    .withTexture("Sampler0", texture, MIPMAP_SAMPLER)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup()));

}
