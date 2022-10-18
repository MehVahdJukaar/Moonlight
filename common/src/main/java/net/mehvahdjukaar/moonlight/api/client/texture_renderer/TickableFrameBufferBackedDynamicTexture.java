package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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