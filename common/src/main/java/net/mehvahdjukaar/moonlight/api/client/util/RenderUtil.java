package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;


public class RenderUtil {

    static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");


    @ExpectPlatform
    public static void renderBlock(BakedModel model, long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                   Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        throw new AssertionError();
    }

    public static void renderBlock(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state,
                                   Level level, BlockPos pos, BlockRenderDispatcher dispatcher) {
        BakedModel model = dispatcher.getBlockModel(state);
        renderBlock(model, seed, poseStack, buffer, state, level, pos, dispatcher);
    }

    @Deprecated(forRemoval = true)
    public static void renderBlockModel(ResourceLocation modelLocation, PoseStack matrixStack, MultiBufferSource buffer,
                                        BlockRenderDispatcher blockRenderer, int light, int overlay, boolean cutout) {
        renderModel(modelLocation, matrixStack, buffer, blockRenderer, light, overlay, cutout);
    }

    //should be a weaker version of what's above as it doesnt take in level so stuff like offset isnt there
    //from resource location
    public static void renderModel(ResourceLocation modelLocation, PoseStack matrixStack, MultiBufferSource buffer,
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
        poseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
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

    @ExpectPlatform
    private static BakedModel handleCameraTransforms(BakedModel model, PoseStack matrixStack, ItemDisplayContext pTransformType) {
        throw new ArrayStoreException();
    }

    @Deprecated(forRemoval = true)
    @ExpectPlatform
    public static void renderGuiItem(BakedModel model, ItemStack stack, ItemRenderer renderer, int combinedLight, int pCombinedOverlay,
                                     PoseStack poseStack, MultiBufferSource.BufferSource buffer, boolean flatItem) {
        throw new ArrayStoreException();
    }

    public static GuiGraphics getGuiDummy(PoseStack poseStack) {
        var mc = Minecraft.getInstance();
        return new GuiGraphics(mc, poseStack, mc.renderBuffers().bufferSource());
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

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light, int index,
                                    int b, int g, int r, TextureAtlasSprite sprite) {
        renderSprite(stack, vertexBuilder, light, index, b, g, r, 255, sprite);
    }

    public static void renderSprite(PoseStack stack, VertexConsumer vertexBuilder, int light, int index,
                                    int b, int g, int r, int a, TextureAtlasSprite sprite) {
        Matrix4f matrix4f1 = stack.last().pose();
        float u0 = sprite.getU(0);
        float u1 = sprite.getU(16);
        float h = (u0 + u1) / 2.0f;
        float v0 = sprite.getV(0);
        float v1 = sprite.getV(16);
        float k = (v0 + v1) / 2.0f;
        float shrink = sprite.uvShrinkRatio();
        float u0s = Mth.lerp(shrink, u0, h);
        float u1s = Mth.lerp(shrink, u1, h);
        float v0s = Mth.lerp(shrink, v0, k);
        float v1s = Mth.lerp(shrink, v1, k);

        vertexBuilder.vertex(matrix4f1, -1.0F, 1.0F, index * -0.001F).color(r, g, b, a).uv(u0s, v1s).uv2(light).endVertex();
        vertexBuilder.vertex(matrix4f1, 1.0F, 1.0F, index * -0.001F).color(r, g, b, a).uv(u1s, v1s).uv2(light).endVertex();
        vertexBuilder.vertex(matrix4f1, 1.0F, -1.0F, index * -0.001F).color(r, g, b, a).uv(u1s, v0s).uv2(light).endVertex();
        vertexBuilder.vertex(matrix4f1, -1.0F, -1.0F, index * -0.001F).color(r, g, b, a).uv(u0s, v0s).uv2(light).endVertex();
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

    public static RenderType getTextColorRenderType(ResourceLocation texture) {
        return MLRenderTypes.COLOR_TEXT.apply(texture);
    }

    /**
     * Call at appropriate times to turn your dynamic textures into mipmapped ones. Remember to turn off
     */
    public static void setDynamicTexturesToUseMipmap(boolean mipMap) {
        MoonlightClient.setMipMap(mipMap);
    }


}

