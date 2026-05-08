package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicTextureRenderer {

    private static final LoadingCache<ResourceLocation,
            CompletableFuture<RenderableDynamicTexture>> TEXTURE_CACHE =
            CacheBuilder.newBuilder()
                    .<ResourceLocation, CompletableFuture<RenderableDynamicTexture>>removalListener(i -> {
                        CompletableFuture<RenderableDynamicTexture> future = i.getValue();
                        if (future == null) return;
                        //this unregisters calls close which makes textureMatrix as closed
                        future.thenAccept(texture ->
                                RenderSystem.recordRenderCall(texture::unregister)
                        );
                    })
                    .expireAfterAccess(2, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public CompletableFuture<RenderableDynamicTexture> load(ResourceLocation key) {
                            return new CompletableFuture<>();
                        }
                    });

    //clears the texture cache and force all to be re-rendered
    public static void clearCache() {
        TEXTURE_CACHE.invalidateAll();
    }

    /**
     * Gets a texture object on which you'll be able to directly draw onto as its in essence a frame buffer
     * Remember to call isInitialized() as the returned texture might be empty
     * For practical purposes you are only interested to call something like buffer.getBuffer(RenderType.entityCutout(texture.getTextureLocation()));
     *
     * @param id id of this texture. must be unique
     **/
    public static <T extends RenderableDynamicTexture> T requestTexture(
            ResourceLocation id,
            Supplier<T> textureSupplier
    ) {
        CompletableFuture<RenderableDynamicTexture> future =
                TEXTURE_CACHE.asMap().computeIfAbsent(id, key -> {
                    CompletableFuture<RenderableDynamicTexture> f = new CompletableFuture<>();

                    RenderSystem.recordRenderCall(() -> {
                        try {
                            T newText = textureSupplier.get();
                            newText.register();
                            newText.redraw();
                            f.complete(newText);
                        } catch (Throwable t) {
                            Moonlight.LOGGER.error("Failed to create dynamic texture for id {}", key, t);
                            f.completeExceptionally(t);
                            TEXTURE_CACHE.invalidate(key);
                        }
                    });

                    return f;
                });

        if (!future.isDone()) {
            return null;
        }

        var t = (T) future.join();
        if (t.isClosed()) {
            TEXTURE_CACHE.invalidate(id);
            Moonlight.LOGGER.error("get texture on closed");
            return null;
        }
        return t;
    }

    /**
     * Gets a texture object on which you'll be able to directly draw onto as its in essence a frame buffer
     * Remember to call isInitialized() as the returned texture might be empty
     * For practical purposes you are only interested to call something like buffer.getBuffer(RenderType.entityCutout(texture.getTextureLocation()));
     *
     * @param id                     id of this texture. must be unique
     * @param textureSize            dimension
     * @param textureDrawingFunction this is the function responsible to draw things onto this texture
     * @return texture instance
     */
    public static RenderableDynamicTexture requestTexture(
            ResourceLocation id, int textureSize,
            @NotNull Consumer<RenderableDynamicTexture> textureDrawingFunction, boolean updateEachFrame) {
        var t = requestTexture(id, () -> new RenderableDynamicTexture(id, textureSize, textureDrawingFunction));
        if (t != null && updateEachFrame) {
            t.setUpdateNextTick(true);
        }
        return t;
    }


    @Nullable
    public static <T extends RenderableDynamicTexture> T getTextureIfPresent(ResourceLocation id) {
        CompletableFuture<RenderableDynamicTexture> ifPresent = TEXTURE_CACHE.getIfPresent(id);
        return ifPresent == null || !ifPresent.isDone() ? null : (T) ifPresent.join();
    }


    public static RenderableDynamicTexture requestFlatItemStackTexture(ResourceLocation res, ItemStack stack, int size) {
        return requestTexture(res, size, t -> drawItem(t, stack), true);
    }

    public static RenderableDynamicTexture requestFlatItemTexture(Item item, int size) {
        return requestFlatItemTexture(item, size, null);
    }

    public static RenderableDynamicTexture requestFlatItemTexture(Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
        ResourceLocation id = Moonlight.res(Utils.getID(item).toString().replace(":", "/") + "/" + size);
        return requestFlatItemTexture(id, item, size, postProcessing, false);
    }

    public static RenderableDynamicTexture requestFlatItemTexture(
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
    public static RenderableDynamicTexture requestFlatItemTexture(
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

    public static void drawItem(RenderableDynamicTexture tex, ItemStack stack) {
        drawAsInGUI(tex, g -> {
            //render stuff
            g.renderFakeItem(stack, 0, 0);
        });
    }

    public static void drawTexture(RenderableDynamicTexture tex, ResourceLocation texture) {
        DynamicTextureRenderer.drawAsInGUI(tex, s -> {
            RenderSystem.setShaderTexture(0, texture);
            PoseStack.Pose pose = s.pose().last();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.addVertex(pose, 0.0f, 16, 0).setUv(0, 0);
            bufferBuilder.addVertex(pose, 16, 16, 0).setUv(1, 0);
            bufferBuilder.addVertex(pose, 16, 0.0f, 0).setUv(1, 1);
            bufferBuilder.addVertex(pose, 0.0f, 0.0f, 0).setUv(0, 1);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        });
    }

    /**
     * Coordinates here are from 0 to 1
     */
    public static void drawNormalized(RenderableDynamicTexture tex, Consumer<PoseStack> drawFunction) {
        drawAsInGUI(tex, g -> {
            var s = g.pose();
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
    public static void drawAsInGUI(RenderableDynamicTexture tex, Consumer<GuiGraphics> drawFunction) {
        //fog bs that idk why its needed with flywheel. MC gui code doesnt need that
        float fogStart = RenderSystem.getShaderFogStart();
        float fogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Integer.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Integer.MAX_VALUE);

        RenderSystem.clear(256, Minecraft.ON_OSX);

        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getRenderTarget();
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
        var posestack = RenderSystem.getModelViewStack();
        posestack.pushMatrix();
        posestack.set(new Matrix4f().identity());

        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code

        //item renderer needs a new pose stack as it applies its last to render system itself. for the rest tbh idk
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        drawFunction.accept(guiGraphics);
        guiGraphics.flush();

        //reset stuff
        posestack.popMatrix();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.restoreProjectionMatrix();
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);

        //and apparently not resetting causes clouds to be messed up
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
    }


}