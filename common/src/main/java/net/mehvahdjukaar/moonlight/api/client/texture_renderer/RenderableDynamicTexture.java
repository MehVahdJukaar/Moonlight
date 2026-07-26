package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

import static net.minecraft.client.Minecraft.ON_OSX;

public class RenderableDynamicTexture extends DynamicTexture implements Tickable {

    //runs when texture is initialized and populates it. Runs each tick if its tickable
    @NotNull
    protected final Consumer<? super RenderableDynamicTexture> drawingFunction;

    //thing that is drawn later
    private RenderTarget readTarget;
    //thing where it renders stuff on
    private RenderTarget writeTarget;

    private final int width;
    private final int height;
    private final ResourceLocation textureLocation;

    private volatile boolean shouldTick = true;
    public boolean closed = false;

    public RenderableDynamicTexture(ResourceLocation resourceLocation, int width, int height,
                                    @NotNull Consumer<? extends RenderableDynamicTexture> textureDrawingFunction) {
        super(width, height, false);
        RenderSystem.assertOnRenderThread();
        this.width = width;
        this.height = height;
        this.textureLocation = resourceLocation;
        this.drawingFunction = (Consumer<? super RenderableDynamicTexture>) textureDrawingFunction;
        this.setUpdateNextTick(true);
    }

    public RenderableDynamicTexture(ResourceLocation resourceLocation, int size,
                                    @NotNull Consumer<? extends RenderableDynamicTexture> textureDrawingFunction) {
        this(resourceLocation, size, size, textureDrawingFunction);
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    private static void renderCall(RenderCall call) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(call);
        } else {
            call.execute();
        }
    }

    /**
     * Force redraw using provided render function. You can also redraw manually
     */
    public void redraw() {
        if (closed) {
            Moonlight.LOGGER.error("redraw on closed");
            return;
        }
        renderCall(() -> {
            bind();
            writeTarget.bindWrite(true);
            drawingFunction.accept(this);
            swapBackToFront();
            writeTarget.unbindWrite();
        });
    }

    @Override
    public void load(ResourceManager manager) {
    }

    // Call after finish drawing
    public void swapBackToFront() {
        //a plain framebuffer copy rather than RenderTarget.blitToScreen: that one masks off the
        //alpha channel, so the front buffer's transparency would stay stuck at its clear value
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, writeTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, readTarget.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, writeTarget.width, writeTarget.height,
                0, 0, readTarget.width, readTarget.height,
                GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public RenderTarget getRenderTarget() {
        return writeTarget;
    }

    //bind read on the current texture
    @Override
    public void bind() {
        if (closed) {
            //TODO: remove these
            Moonlight.LOGGER.error("bind on closed");
            return;
        }
        super.bind();
    }

    //gpu texture ID
    @Override
    public int getId() {
        if (closed) {
            Moonlight.LOGGER.error("get id on closed");
            return 0;
        }
        RenderSystem.assertOnRenderThreadOrInit();
        //needs to be here since the super constructor calls this early
        if (this.readTarget == null || this.writeTarget == null) {
            int w = getPixels().getWidth();
            int h = getPixels().getHeight();
            this.readTarget = new TextureTarget(w, h, false, ON_OSX);
            this.writeTarget = new TextureTarget(w, h, true, ON_OSX);
        }
        //must never change since its just queried when texture is registered
        //this is what binds texture and frame buffers together
        return this.readTarget.getColorTextureId();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    @Override
    public void releaseId() {
        this.closed = true;
        super.releaseId();
        renderCall(() -> {
            if (this.writeTarget != null) {
                this.writeTarget.destroyBuffers();
                this.writeTarget = null;
            }
            if (this.readTarget != null) {
                this.readTarget.destroyBuffers();
                this.readTarget = null;
            }
        });
    }

    /**
     * Downloads the GPU texture to CPU for edit.
     * Works on the back buffer, the one the drawing function renders onto, so this is meant to be
     * called from within it. Anything written back with upload() shows up once it's swapped to front
     */
    public void download() {
        if (closed) {
            Moonlight.LOGGER.error("download id on closed");
            return;
        }
        bindBackBuffer();
        getPixels().downloadTexture(0, false);
    }

    /**
     * Uploads the CPU image back onto the back buffer. Counterpart of download()
     */
    @Override
    public void upload() {
        if (closed) {
            Moonlight.LOGGER.error("upload on closed");
            return;
        }
        bindBackBuffer();
        getPixels().upload(0, 0, 0, false);
    }

    private void bindBackBuffer() {
        this.getId(); //lazily creates the targets if we got here before any bind
        RenderSystem.bindTexture(writeTarget.getColorTextureId());
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

    //safeguard but shouldnt be needed
    public boolean isClosed() {
        return closed;
    }

}