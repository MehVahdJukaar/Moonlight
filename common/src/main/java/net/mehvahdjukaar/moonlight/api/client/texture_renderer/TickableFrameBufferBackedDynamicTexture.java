package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TickableFrameBufferBackedDynamicTexture extends FrameBufferBackedDynamicTexture implements Tickable {


    public TickableFrameBufferBackedDynamicTexture(ResourceLocation resourceLocation, int width, int height,
                                                   @NotNull Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction) {
        super(resourceLocation, width, height, textureDrawingFunction);
    }

    public TickableFrameBufferBackedDynamicTexture(ResourceLocation resourceLocation, int size,
                                                   @NotNull Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction) {
        super(resourceLocation, size, textureDrawingFunction);
    }

    @Override
    public void tick() {
        drawingFunction.accept(this);
    }
}