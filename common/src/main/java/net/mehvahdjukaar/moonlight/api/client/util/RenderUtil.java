package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class RenderUtil {

    //TODO: fix shading so it can be rotated and have consistent shading and is also rendered like a block
    @PlatformImpl
    public static void renderBlockInWorld(BakedModel model, long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                          Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        throw new AssertionError();
    }

    public static void renderBlockInWorld(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                          Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        BakedModel model = dispatcher.getBlockModel(state);
        renderBlockInWorld(model, seed, poseStack, buffer, state, level, pos, dispatcher);
    }

    @Deprecated(forRemoval = true)
    public static void renderBlock(BakedModel model, long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                   Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        renderBlockInWorld(model, seed, poseStack, buffer, state, level, pos, dispatcher);
    }

    @Deprecated(forRemoval = true)
    public static void renderBlock(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                   Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        renderBlockInWorld(seed, poseStack, buffer, state, level, pos, dispatcher);
    }

    /**
     * renders block model like chunks do but without level. not centered same as renderBlockInWorld
     * face shade follows the pose so it still looks right when rotated
     */
    public static void renderBlockDetached(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        renderBlockDetached(model, state, poseStack, buffer, light, overlay);
    }

    @PlatformImpl
    public static void renderBlockDetached(BakedModel model, BlockState state, PoseStack poseStack, MultiBufferSource buffer,
                                           int light, int overlay) {
        throw new AssertionError();
    }

    @ApiStatus.Internal
    public static void putBlockQuads(PoseStack.Pose pose, VertexConsumer consumer, BlockState state,
                                     Function<Direction, List<BakedQuad>> quadsBySide, int light, int overlay) {
        BlockColors colors = Minecraft.getInstance().getBlockColors();
        var level = Minecraft.getInstance().level;
        boolean constantAmbient = level != null && level.effects().constantAmbientLight();
        Vector3f n = new Vector3f();
        for (Direction dir : DIRECTIONS_AND_NULL) {
            for (BakedQuad quad : quadsBySide.apply(dir)) {
                float r = 1, g = 1, b = 1;
                if (quad.isTinted() && state != null) {
                    int tint = colors.getColor(state, null, null, quad.getTintIndex());
                    r = (tint >> 16 & 255) / 255f;
                    g = (tint >> 8 & 255) / 255f;
                    b = (tint & 255) / 255f;
                }
                if (quad.isShade()) {
                    Vec3i normal = quad.getDirection().getNormal();
                    pose.transformNormal(normal.getX(), normal.getY(), normal.getZ(), n).normalize();
                    float shade = getShade(n, constantAmbient);
                    r *= shade;
                    g *= shade;
                    b *= shade;
                }
                consumer.putBulkData(pose, quad, r, g, b, 1, light, overlay);
            }
        }
    }

    //ClientLevel.getShade blended over the normal so rotated faces dont snap between values
    private static float getShade(Vector3f n, boolean constantAmbient) {
        float up = constantAmbient ? 0.9f : 1;
        float down = constantAmbient ? 0.9f : 0.5f;
        float y = n.y * n.y * (n.y > 0 ? up : down);
        return Math.min(n.x * n.x * 0.6f + y + n.z * n.z * 0.8f, 1);
    }

    private static final Direction[] DIRECTIONS_AND_NULL = {Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    // standalone models have no state to get the render type from
    public static void renderModelDetached(ModelResourceLocation modelLocation, PoseStack poseStack, MultiBufferSource buffer,
                                           int light, int overlay, boolean cutout) {
        BakedModel model = ClientHelper.getModel(Minecraft.getInstance().getModelManager(), modelLocation);
        RandomSource random = RandomSource.create();
        VertexConsumer consumer = buffer.getBuffer(cutout ? RenderType.cutout() : RenderType.solid());
        putBlockQuads(poseStack.last(), consumer, null, dir -> {
            random.setSeed(42L);
            return model.getQuads(null, dir, random);
        }, light, overlay);
    }

    //should be a weaker version of what's above as it doesnt take in level so stuff like offset isnt there
    //from resource location
    @Deprecated(forRemoval = true)
    public static void renderModel(ModelResourceLocation modelLocation, PoseStack matrixStack, MultiBufferSource buffer,
                                   BlockRenderDispatcher blockRenderer, int light, int overlay, boolean cutout) {

        blockRenderer.getModelRenderer().renderModel(matrixStack.last(),
                buffer.getBuffer(cutout ? Sheets.cutoutBlockSheet() : Sheets.solidBlockSheet()),
                null,
                ClientHelper.getModel(blockRenderer.getBlockModelShaper().getModelManager(), modelLocation),
                1.0F, 1.0F, 1.0F,
                light, overlay);
    }

    public static void renderGuiItemRelative(PoseStack poseStack, ItemStack stack, int x, int y, ItemRenderer renderer,
                                             BiConsumer<PoseStack, BakedModel> movement) {
        renderGuiItemRelative(poseStack, stack, x, y, renderer, movement, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }


    //im not even using this on fabric...
    public static void renderGuiItemRelative(PoseStack poseStack, ItemStack stack, int x, int y, ItemRenderer renderer,
                                             BiConsumer<PoseStack, BakedModel> movement, int combinedLight, int pCombinedOverlay) {

        BakedModel model = renderer.getModel(stack, null, null, 0);
        int l = 0;

        poseStack.pushPose();

        poseStack.translate((x + 8), (y + 8), (150 + (model.isGui3d() ? l : 0)));
        poseStack.mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        poseStack.scale(16.0F, 16.0F, 16.0F);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        } else {
            Lighting.setupFor3DItems();
        }

        //-----render---
        ItemDisplayContext pTransformType = ItemDisplayContext.GUI;

        // applies rotation first then custom rot and gives display context of none
        model = handleCameraTransforms(model, poseStack, pTransformType);

        //custom rotation

        movement.accept(poseStack, model);

        renderer.render(stack, ItemDisplayContext.NONE, false, poseStack, bufferSource,
                combinedLight, pCombinedOverlay, model);

        //----end-render---

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }
        poseStack.popPose();
    }

    @PlatformImpl
    private static BakedModel handleCameraTransforms(BakedModel model, PoseStack matrixStack, ItemDisplayContext pTransformType) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static GuiGraphics getGuiDummy(PoseStack poseStack) {
        var mc = Minecraft.getInstance();
        var p = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        p.pose().setIdentity();
        p.pose().mulPose(poseStack.last().pose());
        return p;
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
    public static void blitSpriteSection(GuiGraphics graphics, int x, int y, int w, int h,
                                         float u, float v, int uW, int vH, TextureAtlasSprite sprite) {
        var c = sprite.contents();
        int width = (int) (c.width() / (sprite.getU1() - sprite.getU0()));
        int height = (int) (c.height() / (sprite.getV1() - sprite.getV0()));
        graphics.blit(sprite.atlasLocation(), x, y, w, h, sprite.getU(u) * width, height * sprite.getV(v), uW, vH, width, height);
    }

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light,
                                    int b, int g, int r, TextureAtlasSprite sprite) {
        renderSprite(stack, vertexBuilder, light, b, g, r, 255, sprite);
    }

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light,
                                    int b, int g, int r, int a, TextureAtlasSprite sprite) {
        Matrix4f matrix4f1 = stack.last().pose();
        float u0 = sprite.getU(0);
        float u1 = sprite.getU(1);
        float h = (u0 + u1) / 2.0f;
        float v0 = sprite.getV(0);
        float v1 = sprite.getV(1);
        float k = (v0 + v1) / 2.0f;
        float shrink = sprite.uvShrinkRatio();
        float u0s = Mth.lerp(shrink, u0, h);
        float u1s = Mth.lerp(shrink, u1, h);
        float v0s = Mth.lerp(shrink, v0, k);
        float v1s = Mth.lerp(shrink, v1, k);

        vertexBuilder.addVertex(matrix4f1, -1.0F, 1.0F, 0).setColor(r, g, b, a).setUv(u0s, v1s).setLight(light);
        vertexBuilder.addVertex(matrix4f1, 1.0F, 1.0F, 0).setColor(r, g, b, a).setUv(u1s, v1s).setLight(light);
        vertexBuilder.addVertex(matrix4f1, 1.0F, -1.0F, 0).setColor(r, g, b, a).setUv(u1s, v0s).setLight(light);
        vertexBuilder.addVertex(matrix4f1, -1.0F, -1.0F, 0).setColor(r, g, b, a).setUv(u0s, v0s).setLight(light);
    }


    /**
     * Text render type that can use mipmap.
     */
    public static RenderType getTextMipmapRenderType(ResourceLocation texture) {
        return MLRenderTypes.TEXT_MIP.apply(texture);
    }

    public static RenderType getEntityCutoutMipmapRenderType(ResourceLocation texture) {
        return MLRenderTypes.ENTITY_CUTOUT_MIP.apply(texture);
    }

    public static RenderType getEntitySolidMipmapRenderType(ResourceLocation texture) {
        return MLRenderTypes.ENTITY_SOLID_MIP.apply(texture);
    }

    /**
     * A render type that colors a texture entirely using the shader color. Just takes the shape of it into account (non transparent pixels)
     */
    public static RenderType getColoredTextureRenderType(ResourceLocation texture) {
        return MLRenderTypes.COLOR_TEXT.apply(texture);
    }

    /**
     * Call at appropriate times to turn your dynamic textures into mipmapped ones. Remember to turn off
     */
    public static void setDynamicTexturesToUseMipmap(boolean mipMap) {
        MoonlightClient.setMipMap(mipMap);
    }


    public static ModelResourceLocation getStandaloneModelLocation(ResourceLocation location) {
        return new ModelResourceLocation(location, PlatHelper.getPlatform().isFabric() ? "fabric_resource" : "standalone");
    }

}

