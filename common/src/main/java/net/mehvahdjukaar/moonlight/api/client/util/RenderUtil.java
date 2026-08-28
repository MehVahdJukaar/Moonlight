package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.gui.AnimatedGuiItem;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;


public class RenderUtil {

    private static final int[] NO_TINTS = new int[0];

    /** Draws a block state like a falling block: level lighting, ao and biome tints resolved at pos. */
    public static void submitBlock(PoseStack poseStack, SubmitNodeCollector collector, BlockState state,
                                   @Nullable ClientLevel level, BlockPos pos, BlockPos randomSeedPos) {
        MovingBlockRenderState renderState = new MovingBlockRenderState();
        renderState.blockState = state;
        renderState.blockPos = pos;
        renderState.randomSeedPos = randomSeedPos;
        if (level != null) {
            renderState.biome = level.getBiome(pos);
            renderState.cardinalLighting = level.cardinalLighting();
            renderState.lightEngine = level.getLightEngine();
        }
        collector.submitMovingBlock(poseStack, renderState);
    }

    /** Draws a block model with an explicit light value, no level lighting or tints. */
    public static void submitBlockModel(PoseStack poseStack, SubmitNodeCollector collector, BlockStateModel model,
                                        @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                        @Nullable BlockState state, RandomSource random, boolean cutout,
                                        int light, int overlay) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        ClientHelper.collectModelParts(model, level, pos, state, random, parts);
        RenderType renderType = cutout ? Sheets.cutoutBlockSheet() : Sheets.translucentBlockSheet();
        collector.submitBlockModel(poseStack, renderType, parts, NO_TINTS, light, overlay, 0);
    }

    public static void renderGuiItemRelative(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int size,
                                             AnimatedGuiItem.Transform transform) {
        AnimatedGuiItem.submit(graphics, stack, x, y, size, -1, transform);
    }

    /**
     * Renders the given sprite or sprite section. Meant for GUI
     *
     * @param x      x position
     * @param y      y position
     * @param w      width
     * @param h      height
     * @param u      sprite local u
     * @param v      sprite local v
     * @param uW     sprite section width
     * @param vH     sprite section height
     * @param sprite can be grabbed from a material
     */
    public static void blitSpriteSection(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
                                         float u, float v, int uW, int vH, TextureAtlasSprite sprite) {
        var c = sprite.contents();
        int width = (int) (c.width() / (sprite.getU1() - sprite.getU0()));
        int height = (int) (c.height() / (sprite.getV1() - sprite.getV0()));
        graphics.blit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x, y,
                sprite.getU(u) * width, sprite.getV(v) * height, w, h, uW, vH, width, height);
    }

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light,
                                    int b, int g, int r, TextureAtlasSprite sprite) {
        renderSprite(stack, vertexBuilder, light, b, g, r, 255, sprite);
    }

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light,
                                    int b, int g, int r, int a, TextureAtlasSprite sprite) {
        renderSprite(stack.last(), vertexBuilder, light, ARGB.color(a, r, g, b), sprite);
    }

    /**
     * Draws a sprite as a unit quad centered on the origin, facing +Z
     */
    public static void renderSprite(PoseStack.Pose pose, VertexConsumer vertexBuilder, int light,
                                    int color, TextureAtlasSprite sprite) {
        float u0s = sprite.getU0();
        float u1s = sprite.getU1();
        float v0s = sprite.getV0();
        float v1s = sprite.getV1();

        vertexBuilder.addVertex(pose, -1.0F, 1.0F, 0).setColor(color).setUv(u0s, v1s).setLight(light);
        vertexBuilder.addVertex(pose, 1.0F, 1.0F, 0).setColor(color).setUv(u1s, v1s).setLight(light);
        vertexBuilder.addVertex(pose, 1.0F, -1.0F, 0).setColor(color).setUv(u1s, v0s).setLight(light);
        vertexBuilder.addVertex(pose, -1.0F, -1.0F, 0).setColor(color).setUv(u0s, v0s).setLight(light);
    }


    /**
     * Text render type that can use mipmap.
     */
    public static RenderType getTextMipmapRenderType(Identifier texture) {
        return MLRenderTypes.TEXT_MIP.apply(texture);
    }

    public static RenderType getEntityCutoutMipmapRenderType(Identifier texture) {
        return MLRenderTypes.ENTITY_CUTOUT_MIP.apply(texture);
    }

    public static RenderType getEntitySolidMipmapRenderType(Identifier texture) {
        return MLRenderTypes.ENTITY_SOLID_MIP.apply(texture);
    }

    /**
     * A render type that colors a texture entirely using the vertex color. Just takes the shape of it into account (non transparent pixels)
     */
    public static RenderType getColoredTextureRenderType(Identifier texture) {
        return MLRenderTypes.COLOR_TEXT.apply(texture);
    }

    /**
     * Return this from SingleQuadParticle.getLayer for a
     * block atlas particle that blends additively. Vanilla only has opaque and translucent ones
     */
    public static SingleQuadParticle.Layer getAdditiveParticleLayer() {
        return MLRenderTypes.ADDITIVE_TERRAIN_PARTICLE_LAYER;
    }

}

