package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;


public class RenderUtil {

    static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");

    @ExpectPlatform
    public static void renderBlock(long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate, Level world, BlockPos blockpos, BlockRenderDispatcher blockRenderer) {
        throw new AssertionError();
    }

    //from resource location
    public static void renderBlockModel(ResourceLocation modelLocation, PoseStack matrixStack, MultiBufferSource buffer,
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
        poseStack.translate(0.0F, 0.0F, (50 + (model.isGui3d() ? l : 0)));

        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(x, y, 100.0F  + 50.0F);
        poseStack.translate(8.0D, 8.0D, 0.0D);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);


        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }else{
            Lighting.setupFor3DItems();
        }

        //-----render---

        ItemDisplayContext pTransformType = ItemDisplayContext.GUI;


        if (stack.is(Items.TRIDENT)) {
            model = renderer.getItemModelShaper().getModelManager().getModel(TRIDENT_MODEL);
        } else if (stack.is(Items.SPYGLASS)) {
            model = renderer.getItemModelShaper().getModelManager().getModel(SPYGLASS_MODEL);
        }

        model = handleCameraTransforms(model, poseStack, pTransformType);

        //custom rotation

        movement.accept(poseStack, model);

        renderGuiItem(model, stack, renderer, combinedLight, pCombinedOverlay, poseStack, bufferSource, flag);


        //----end-render---

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();

        poseStack.popPose();
    }

    @ExpectPlatform
    private static BakedModel handleCameraTransforms(BakedModel model, PoseStack matrixStack, ItemDisplayContext pTransformType) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static void renderGuiItem(BakedModel model, ItemStack stack, ItemRenderer renderer, int combinedLight, int pCombinedOverlay,
                                     PoseStack poseStack, MultiBufferSource.BufferSource buffer, boolean flatItem) {
        throw new ArrayStoreException();
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
    public static void blitSprite(PoseStack matrixStack, int x, int y, int w, int h,
                                  float u, float v, int uW, int vH, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        var c = sprite.contents();
        int width = (int) (c.width() / (sprite.getU1() - sprite.getU0()));
        int height = (int) (c.height() / (sprite.getV1() - sprite.getV0()));
        GuiComponent.blit(matrixStack, x, y, w, h, sprite.getU(u) * width, height * sprite.getV(v), uW, vH, width, height);
    }


}

