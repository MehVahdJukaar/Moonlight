package net.mehvahdjukaar.selene.client.texture_renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.mehvahdjukaar.selene.Selene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FlatItemTextureManager {

    private record RenderingData(ResourceLocation id, Item item, @Nullable Consumer<NativeImage> postProcessing) {
    }

    private static final Queue<RenderingData> REQUESTED_FOR_RENDERING = new ArrayDeque<>();

    private static final LoadingCache<ResourceLocation, FrameBufferBackedDynamicTexture> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(4, TimeUnit.MINUTES)
            .removalListener(i -> {
                FrameBufferBackedDynamicTexture value = (FrameBufferBackedDynamicTexture) i.getValue();
                if (value != null) value.close();
            })
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

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size, String prefix) {
        return getFlatItemTexture(item, size, prefix, null);
    }

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size, String prefix, @Nullable Consumer<NativeImage> postProcessing) {
        //texture id for item size pair
        ResourceLocation res = Selene.res(item.getRegistryName().toString().replace(":", "/")
                + "/" + size + "/" + prefix);
        var texture = TEXTURE_CACHE.getIfPresent(res);
        if (texture == null) {
            texture = new FrameBufferBackedDynamicTexture(res, size);
            TEXTURE_CACHE.put(res, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            REQUESTED_FOR_RENDERING.add(new RenderingData(res, item, postProcessing));
        }
        return texture;
    }

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size) {
        return getFlatItemTexture(item, size, "");
    }

    //called each rendering tick
    public static void updateTextures() {
        while (true) {
            var b = REQUESTED_FOR_RENDERING.poll();
            if (b == null) return;
            ResourceLocation res = b.id;
            var texture = TEXTURE_CACHE.getIfPresent(res);
            if (texture != null) {
                drawItem(texture, b.item);
                if (b.postProcessing != null) {
                    texture.download();
                    NativeImage img = texture.getPixels();
                    b.postProcessing.accept(img);
                    texture.upload();
                }
            }
        }
    }

    public static void drawItem(FrameBufferBackedDynamicTexture tex, Item item) {

        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);
        //render to this one
        frameBuffer.bindWrite(true);

        int size = 16;

        //gui setup code
        RenderSystem.clear(256, Minecraft.ON_OSX);

        Matrix4f oldProjection = RenderSystem.getProjectionMatrix();

        Matrix4f matrix4f = Matrix4f.orthographic(0.0F,
                size, 0, size, 1000.0F, ForgeHooksClient.getGuiFarPlane());
        RenderSystem.setProjectionMatrix(matrix4f);

        //model view stuff
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, 1000F - ForgeHooksClient.getGuiFarPlane());
        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code


        //render stuff
        ItemRenderer itemRenderer = mc.getItemRenderer();
        //Minecraft.getInstance().gui.render(posestack,1);
        itemRenderer.renderGuiItem(item.getDefaultInstance(), 0, 0);
/*
        RenderSystem.setShaderTexture(0,
                new ResourceLocation("textures/gui/container/villager2.png")
       );
        Gui.blit(posestack,0,0,1000,0,0,
                256,256,16,16);
*/
        //reset stuff
        posestack.popPose();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.setProjectionMatrix(oldProjection);

       // RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);

    }
}
