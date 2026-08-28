package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cache of RenderableDynamicTextures by id. Textures expire after a couple of minutes without use,
 * so request them by id each time instead of holding the instance.
 */
public class DynamicTextureRenderer {

    private static final float GUI_BOX = 16;

    private static final Projection PROJECTION = new Projection();
    @Nullable
    private static ProjectionMatrixBuffer projectionBuffer = null;

    private static final Cache<Identifier, RenderableDynamicTexture> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .removalListener((RemovalListener<Identifier, RenderableDynamicTexture>) notification -> {
                RenderableDynamicTexture texture = notification.getValue();
                if (texture != null) onRenderThread(texture::unregister);
            })
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    public static void clearCache() {
        TEXTURE_CACHE.invalidateAll();
    }

    private static void onRenderThread(Runnable task) {
        if (RenderSystem.isOnRenderThread()) task.run();
        else Minecraft.getInstance().execute(task);
    }

    /** Gets or creates the texture, drawing it the first time. Null if creating it failed. */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends RenderableDynamicTexture> T requestTexture(Identifier id, Supplier<T> textureSupplier) {
        RenderSystem.assertOnRenderThread();
        RenderableDynamicTexture texture = TEXTURE_CACHE.asMap().computeIfAbsent(id, key -> {
            try {
                T newTexture = textureSupplier.get();
                newTexture.register();
                newTexture.redraw();
                return newTexture;
            } catch (Throwable t) {
                Moonlight.LOGGER.error("Failed to create dynamic texture for id {}", key, t);
                return null;
            }
        });
        if (texture == null) return null;
        if (texture.isClosed()) {
            //closed elsewhere, drop it
            TEXTURE_CACHE.invalidate(id);
            return null;
        }
        return (T) texture;
    }

    @Nullable
    public static RenderableDynamicTexture requestTexture(
            Identifier id, int textureSize,
            @NotNull Consumer<RenderableDynamicTexture> textureDrawingFunction, boolean updateEachFrame) {
        var t = requestTexture(id, () -> new RenderableDynamicTexture(id, textureSize, textureDrawingFunction));
        if (t != null && updateEachFrame) {
            t.setUpdateNextTick(true);
        }
        return t;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends RenderableDynamicTexture> T getTextureIfPresent(Identifier id) {
        return (T) TEXTURE_CACHE.getIfPresent(id);
    }


    @Nullable
    public static RenderableDynamicTexture requestFlatItemStackTexture(Identifier res, ItemStack stack, int size) {
        return requestTexture(res, size, t -> drawItem(t, stack), true);
    }

    @Nullable
    public static RenderableDynamicTexture requestFlatItemTexture(Item item, int size) {
        return requestFlatItemTexture(item, size, null);
    }

    @Nullable
    public static RenderableDynamicTexture requestFlatItemTexture(Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
        Identifier id = Moonlight.res(Utils.getID(item).toString().replace(":", "/") + "/" + size);
        return requestFlatItemTexture(id, item, size, postProcessing, false);
    }

    @Nullable
    public static RenderableDynamicTexture requestFlatItemTexture(
            Identifier id, Item item, int size, @Nullable Consumer<NativeImage> postProcessing) {
        return requestFlatItemTexture(id, item, size, postProcessing, false);
    }

    /** postProcessing runs on the native image and stalls on a gpu readback, so it's slow. */
    @Nullable
    public static RenderableDynamicTexture requestFlatItemTexture(
            Identifier id, Item item, int size,
            @Nullable Consumer<NativeImage> postProcessing, boolean updateEachFrame) {
        return requestTexture(id, size, t -> {
            drawItem(t, item.getDefaultInstance());
            if (postProcessing != null) {
                t.download();
                postProcessing.accept(t.getPixels());
                t.upload();
            }
        }, updateEachFrame);
    }


    //Utility methods

    public static void drawItem(RenderableDynamicTexture tex, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        drawAsInGUI(tex, (pose, collector) -> {
            ItemStackRenderState itemState = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
            mc.gameRenderer.getLighting().setupFor(itemState.usesBlockLight() ?
                    Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

            pose.translate(GUI_BOX / 2, GUI_BOX / 2, 0);
            pose.scale(GUI_BOX, -GUI_BOX, GUI_BOX);
            itemState.submit(pose, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        });
    }

    /** Same as drawAsInGUI but with coordinates from 0 to 1 instead of 0 to 16. */
    public static void drawNormalized(RenderableDynamicTexture tex, BiConsumer<PoseStack, SubmitNodeCollector> drawFunction) {
        drawAsInGUI(tex, (pose, collector) -> {
            pose.scale(GUI_BOX, GUI_BOX, 1);
            drawFunction.accept(pose, collector);
        });
    }

    /** Draws with a gui-like setup: a 0 to 16 box, origin top left, item lighting. Flushes before returning. */
    public static void drawAsInGUI(RenderableDynamicTexture tex, BiConsumer<PoseStack, SubmitNodeCollector> drawFunction) {
        RenderSystem.assertOnRenderThread();
        Minecraft mc = Minecraft.getInstance();
        if (projectionBuffer == null) projectionBuffer = new ProjectionMatrixBuffer("moonlight dynamic texture");

        RenderTarget target = tex.getRenderTarget();
        GpuTextureView oldColor = RenderSystem.outputColorTextureOverride;
        GpuTextureView oldDepth = RenderSystem.outputDepthTextureOverride;
        RenderSystem.outputColorTextureOverride = target.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();

        RenderSystem.backupProjectionMatrix();
        PROJECTION.setupOrtho(-1000, 1000, GUI_BOX, GUI_BOX, true);
        RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(PROJECTION), ProjectionType.ORTHOGRAPHIC);
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        try {
            drawFunction.accept(new PoseStack(), dispatcher.getSubmitNodeStorage());
            dispatcher.renderAllFeatures();
            mc.renderBuffers().bufferSource().endBatch();
        } finally {
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.outputColorTextureOverride = oldColor;
            RenderSystem.outputDepthTextureOverride = oldDepth;
        }
    }
}
