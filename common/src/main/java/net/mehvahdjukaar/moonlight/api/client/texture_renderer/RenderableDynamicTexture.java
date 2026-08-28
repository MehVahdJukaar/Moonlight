package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * A texture drawn by a drawing function instead of loaded from a resource pack. Once registered its id
 * works like any texture id. DynamicTextureRenderer has helpers and a cache.
 */
public class RenderableDynamicTexture extends AbstractTexture implements TickableTexture {

    //runs when the texture is initialized and populates it. Runs again each tick if it's set to
    @NotNull
    protected final Consumer<? super RenderableDynamicTexture> drawingFunction;

    private final Identifier textureLocation;
    private final TextureTarget target;

    //cpu copy, only allocated if download() is ever called
    @Nullable
    private NativeImage pixels;

    private volatile boolean shouldTick = true;
    private boolean closed = false;

    @SuppressWarnings("unchecked")
    public RenderableDynamicTexture(Identifier resourceLocation, int width, int height,
                                    @NotNull Consumer<? extends RenderableDynamicTexture> textureDrawingFunction) {
        RenderSystem.assertOnRenderThread();
        this.textureLocation = resourceLocation;
        this.drawingFunction = (Consumer<? super RenderableDynamicTexture>) textureDrawingFunction;
        //depth is needed for 3d block models to sort against themselves
        this.target = new TextureTarget(resourceLocation.toString(), width, height, true);
        //share the target's color attachment as this texture
        this.texture = target.getColorTexture();
        this.textureView = target.getColorTextureView();
    }

    public RenderableDynamicTexture(Identifier resourceLocation, int size,
                                    @NotNull Consumer<? extends RenderableDynamicTexture> textureDrawingFunction) {
        this(resourceLocation, size, size, textureDrawingFunction);
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }

    public RenderTarget getRenderTarget() {
        return target;
    }

    public int getWidth() {
        return target.width;
    }

    public int getHeight() {
        return target.height;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Runs the drawing function into this texture. Called on creation and on ticks after setUpdateNextTick.
     */
    public void redraw() {
        if (closed) {
            Moonlight.LOGGER.error("Tried to redraw closed dynamic texture {}", textureLocation);
            return;
        }
        RenderSystem.assertOnRenderThread();
        RenderSystem.getDevice().createCommandEncoder()
                .clearColorAndDepthTextures(target.getColorTexture(), 0, target.getDepthTexture(), 1);

        GpuTextureView oldColor = RenderSystem.outputColorTextureOverride;
        GpuTextureView oldDepth = RenderSystem.outputDepthTextureOverride;
        RenderSystem.outputColorTextureOverride = target.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();
        try {
            drawingFunction.accept(this);
        } finally {
            RenderSystem.outputColorTextureOverride = oldColor;
            RenderSystem.outputDepthTextureOverride = oldDepth;
        }
    }

    /**
     * Cpu copy, valid after download. Edit it and push it back with upload.
     */
    public NativeImage getPixels() {
        if (this.pixels == null) {
            this.pixels = new NativeImage(getWidth(), getHeight(), false);
        }
        return this.pixels;
    }

    /**
     * Reads the texture back into getPixels. Blocks on the gpu, don't call it per frame.
     * Rows come back bottom up, upload is the inverse.
     */
    public void download() {
        if (closed) {
            Moonlight.LOGGER.error("Tried to download closed dynamic texture {}", textureLocation);
            return;
        }
        RenderSystem.assertOnRenderThread();
        GpuTexture color = target.getColorTexture();
        int width = target.width;
        int height = target.height;
        int pixelSize = color.getFormat().pixelSize();
        NativeImage image = getPixels();

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Moonlight texture readback",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) width * height * pixelSize)) {
            encoder.copyTextureToBuffer(color, buffer, 0, () -> {
            }, 0);
            awaitGpu(encoder);
            try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, true, false)) {
                ByteBuffer data = view.data();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        image.setPixelABGR(x, y, data.getInt((x + y * width) * pixelSize));
                    }
                }
            }
        }
    }

    public void upload() {
        if (closed || pixels == null) return;
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(target.getColorTexture(), pixels);
    }

    //the completion callback of copyTextureToBuffer is only polled once a frame, and mapping right after the
    //copy races the draws feeding it, so block on our own fence instead. awaitCompletion doesn't flush by itself
    private static void awaitGpu(CommandEncoder encoder) {
        try (GpuFence fence = encoder.createFence()) {
            GL11.glFlush();
            //noinspection StatementWithEmptyBody
            while (!fence.awaitCompletion(1_000_000_000L)) {
            }
        }
    }

    public void setUpdateNextTick(boolean shouldTick) {
        this.shouldTick = shouldTick;
    }

    @ApiStatus.Internal
    @Override
    public void tick() {
        if (!shouldTick) return;
        shouldTick = false;
        redraw();
    }

    public void register() {
        Minecraft.getInstance().getTextureManager().register(textureLocation, this);
    }

    public void unregister() {
        //this also calls close
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        AbstractTexture t = tm.getTexture(textureLocation);
        //if it's us we release it. Otherwise it means we have already been closed
        if (t == this) {
            tm.release(textureLocation);
        }
    }

    @Override
    public void close() {
        this.closed = true;
        //the render target owns the texture, null ours so AbstractTexture#close doesn't double free it
        this.texture = null;
        this.textureView = null;
        this.target.destroyBuffers();
        if (this.pixels != null) {
            this.pixels.close();
            this.pixels = null;
        }
    }
}
