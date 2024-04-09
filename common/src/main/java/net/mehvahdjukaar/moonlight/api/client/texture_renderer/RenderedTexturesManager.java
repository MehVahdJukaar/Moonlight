package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RenderedTexturesManager {

    private static final LoadingCache<ResourceLocation, FrameBufferBackedDynamicTexture> TEXTURE_CACHE =
            CacheBuilder.newBuilder()
                    .removalListener(i -> {
                        //REQUEST_FOR_CLOSING.add((FrameBufferBackedDynamicTexture) i.getValue())
                        RenderSystem.recordRenderCall(((FrameBufferBackedDynamicTexture) i.getValue())::close);
                    })
                    .expireAfterAccess(2, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public FrameBufferBackedDynamicTexture load(ResourceLocation key) {
                            return null;
                        }
                    });

    //clears the texture cache and forge all to be re-rendered
    public static void clearCache() {
        TEXTURE_CACHE.invalidateAll();
    }

    /**
     * Gets a texture object on which you'll be able to directly draw onto as its in essence a frame buffer
     * Remember to call isInitialized() as the returned texture might be empty
     * For practical purposes you are only interested to call something like buffer.getBuffer(RenderType.entityCutout(texture.getTextureLocation()));
     *
     * @param id                     id of this texture. must be unique
     * @param textureSize            dimension
     * @param textureDrawingFunction this is the function responsible to draw things onto this texture
     * @param updateEachFrame        if this texture should be redrawn each frame. Useful if you are drawing an entity or animated item
     * @return texture instance
     */
    public static FrameBufferBackedDynamicTexture requestTexture(
            ResourceLocation id, int textureSize,
            Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction,
            boolean updateEachFrame) {

        var texture = TEXTURE_CACHE.getIfPresent(id);
        if (texture == null) {
            texture = updateEachFrame ?
                    new TickableFrameBufferBackedDynamicTexture(id, textureSize, textureDrawingFunction) :
                    new FrameBufferBackedDynamicTexture(id, textureSize, textureDrawingFunction);
            TEXTURE_CACHE.put(id, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            //REQUESTED_FOR_RENDERING.add(texture);

            RenderSystem.recordRenderCall(texture::initialize);
        }
        return texture;
    }

    public static FrameBufferBackedDynamicTexture requestFlatItemStackTexture(ResourceLocation res, ItemStack stack, int size) {
        return requestTexture(res, size, t -> drawItem(t, stack), true);
    }

    public static FrameBufferBackedDynamicTexture requestFlatItemTexture(Item item, int size) {
        return requestFlatItemTexture(item, size, null);
    }

    public static FrameBufferBackedDynamicTexture requestFlatItemTexture(Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
        ResourceLocation id = Moonlight.res(Utils.getID(item).toString().replace(":", "/") + "/" + size);
        return requestFlatItemTexture(id, item, size, postProcessing, false);
    }

    public static FrameBufferBackedDynamicTexture requestFlatItemTexture(
            ResourceLocation id, Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
        return requestFlatItemTexture(id, item, size, postProcessing, false);
    }

    /**
     * Draws a flax GUI-like item onto this texture with the given size
     *
     * @param item           item you want to draw
     * @param size           texture size
     * @param id             texture id. Needs to be unique
     * @param postProcessing some extra drawing functions to be applied on the native image. Can be slow as its cpu sided
     */
    public static FrameBufferBackedDynamicTexture requestFlatItemTexture(
            ResourceLocation id, Item item, int size,
            @Nullable Consumer<NativeImage> postProcessing, boolean updateEachFrame) {
        return requestTexture(id, size, t -> {
            drawItem(t, item.getDefaultInstance());
            if (postProcessing != null) {
                t.download();
                NativeImage img = t.getPixels();
                postProcessing.accept(img);
                t.upload();
            }
        }, updateEachFrame);
    }


    //Utility methods

    public static void drawItem(FrameBufferBackedDynamicTexture tex, ItemStack stack) {
        drawAsInGUI(tex, s -> {
            //render stuff
            RenderUtil.getGuiDummy(s).renderFakeItem(stack, 0, 0);
        });
    }

    public static void drawTexture(FrameBufferBackedDynamicTexture tex, ResourceLocation texture) {
        RenderedTexturesManager.drawAsInGUI(tex, s -> {
            RenderSystem.setShaderTexture(0, texture);
            var matrix = s.last().pose();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix, 0.0f, 16, 0).uv(0, 0).endVertex();
            bufferBuilder.vertex(matrix, 16, 16, 0).uv(1, 0).endVertex();
            bufferBuilder.vertex(matrix, 16, 0.0f, 0).uv(1, 1).endVertex();
            bufferBuilder.vertex(matrix, 0.0f, 0.0f, 0).uv(0, 1).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
        });
    }

    /**
     * Coordinates here are from 0 to 1
     */
    public static void drawNormalized(FrameBufferBackedDynamicTexture tex, Consumer<PoseStack> drawFunction) {
        drawAsInGUI(tex, s -> {
            float scale = 1f / 16f;
            s.translate(8, 8, 0);
            s.scale(scale, scale, 1);
            drawFunction.accept(s);
        });
    }

    /**
     * Utility method that sets up an environment akin to gui rendering with a box from 0 t0 16.
     * If you render an item at 0,0 it will be centered
     */
    public static void drawAsInGUI(FrameBufferBackedDynamicTexture tex, Consumer<PoseStack> drawFunction) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);

        //render to this one
        frameBuffer.bindWrite(true);

        int size = 16;
        //save old projection and sets new orthographic
        RenderSystem.backupProjectionMatrix();
        //like this so object center is exactly at 0 0 0
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, size, size, 0, -1000.0F, 1000);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);

        //model view stuff
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();

        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code

        //item renderer needs a new pose stack as it applies its last to render system itself. for the rest tbh idk
        drawFunction.accept(new PoseStack());

        //reset stuff
        posestack.popPose();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.restoreProjectionMatrix();
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);
    }


}