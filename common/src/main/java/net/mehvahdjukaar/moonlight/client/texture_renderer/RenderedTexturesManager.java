package net.mehvahdjukaar.moonlight.client.texture_renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RenderedTexturesManager {

    private record RenderingData(
            ResourceLocation id,
            Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction,
            boolean animated) {
    }

    private static final List<RenderingData> REQUESTED_FOR_RENDERING = new ArrayList<>();

    private static final LoadingCache<ResourceLocation, FrameBufferBackedDynamicTexture> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
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

    public static FrameBufferBackedDynamicTexture getRenderedTexture(
            ResourceLocation res, int size,
            Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction,
            boolean updateEachFrame) {
        var texture = TEXTURE_CACHE.getIfPresent(res);
        if (texture == null) {
            texture = new FrameBufferBackedDynamicTexture(res, size);
            TEXTURE_CACHE.put(res, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            REQUESTED_FOR_RENDERING.add(new RenderingData(res, textureDrawingFunction, updateEachFrame));
        }
        return texture;
    }

    public static FrameBufferBackedDynamicTexture getFlatItemStackTexture(ResourceLocation res, ItemStack stack, int size) {
        return getRenderedTexture(res, size, t -> drawItem(t, stack), true);
    }

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size, String prefix) {
        return getFlatItemTexture(item, size, prefix, null);
    }

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size, String prefix, @Nullable Consumer<NativeImage> postProcessing) {
        //texture id for item size pair
        if (!prefix.isEmpty()) prefix = "/" + prefix;
        ResourceLocation res = Moonlight.res(Utils.getID(item).toString().replace(":", "/")
                + "/" + size + prefix);

        var texture = TEXTURE_CACHE.getIfPresent(res);
        if (texture == null) {
            texture = new FrameBufferBackedDynamicTexture(res, size);
            TEXTURE_CACHE.put(res, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            Consumer<FrameBufferBackedDynamicTexture> factory = t -> {
                drawItem(t, item.getDefaultInstance());
                //drawItem2(texture,new BlockPos(0,70,0),Direction.NORTH,1);
                if (postProcessing != null) {
                    t.download();
                    NativeImage img = t.getPixels();
                    postProcessing.accept(img);
                    t.upload();
                }
            };
            REQUESTED_FOR_RENDERING.add(new RenderingData(res, factory, false));
        }
        return texture;
    }

    public static FrameBufferBackedDynamicTexture getFlatItemTexture(Item item, int size) {
        return getFlatItemTexture(item, size, "");
    }

    //called each rendering tick
    public static void updateTextures() {
        ListIterator<RenderingData> iter = REQUESTED_FOR_RENDERING.listIterator();
        while (iter.hasNext()) {
            var data = iter.next();
            var texture = TEXTURE_CACHE.getIfPresent(data.id);
            if (texture != null) {
                texture.initialized = true;
                data.textureDrawingFunction.accept(texture);
            }
            if (!data.animated || texture == null) {
                iter.remove();
            }
        }
    }

    public static void drawItem2(FrameBufferBackedDynamicTexture tex, BlockPos mirrorPos, Direction mirrorDir, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);
        //render to this one
        frameBuffer.bindWrite(true);

        //gui setup code
        // RenderSystem.clear(256, Minecraft.ON_OSX);

        // Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
        // PoseStack posestack = RenderSystem.getModelViewStack();
        //  posestack.pushPose();
        int size = tex.getWidth();

        GameRenderer gameRenderer = mc.gameRenderer;
        LevelRenderer levelRenderer = mc.levelRenderer;

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(16640, Minecraft.ON_OSX);

        FogRenderer.setupNoFog();

        RenderSystem.enableTexture();
        RenderSystem.enableCull();

        RenderSystem.viewport(0, 0, size, size);
        gameRenderer.renderZoomed(1, 0, 0);
        levelRenderer.doEntityOutline();
        //RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        // gameRenderer.renderLevel(partialTicks,Util.getMillis(),new PoseStack());
        /*
        {

            PoseStack poseStack = new PoseStack();


            RenderSystem.viewport(0, 0, size, size);

           // DummyCamera camera = new DummyCamera();
            //camera.setPosition(mirrorPos);
           // camera.setAnglesInternal(0, -180);
Camera camera = gameRenderer.getMainCamera();

            gameRenderer.lightTexture().updateLightTexture(partialTicks);

            //Camera camera = this.mainCamera;

            //this.renderDistance = (float)(this.minecraft.options.getEffectiveRenderDistance() * 16);
            PoseStack posestack1 = new PoseStack();
            // double d0 = this.getFov(camera, partialTicks, true);
            float fov = 70;
            int renderDistance = 30;
            posestack1.last().pose().multiply(getProjectionMatrix(fov, size, renderDistance));


            Matrix4f matrix4f = posestack1.last().pose();
            RenderSystem.setProjectionMatrix(matrix4f);
            //camera.setup(this.minecraft.level, (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), pPartialTicks);

            //camera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(0));

            poseStack.mulPose(Vector3f.XP.rotationDegrees(-180));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(0 + 180.0F));
            Matrix3f matrix3f = poseStack.last().normal().copy();
            if (matrix3f.invert()) {
                RenderSystem.setInverseViewRotationMatrix(matrix3f);
            }

            levelRenderer.prepareCullFrustum(poseStack, camera.getPosition(),
                    getProjectionMatrix(Math.max(fov, mc.options.fov), size, renderDistance));
            levelRenderer.renderLevel(poseStack, partialTicks, Util.getNanos(), false, camera,
                    gameRenderer, gameRenderer.lightTexture(), matrix4f);

        }*/
        posestack.popPose();

        RenderSystem.applyModelViewMatrix();

        //reset projection
        //  RenderSystem.setProjectionMatrix(oldProjection);

        // RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);

    }

    public static Matrix4f getProjectionMatrix(double pFov, int size, int renderDistance) {
        PoseStack posestack = new PoseStack();
        posestack.last().pose().setIdentity();
        float zoom = 1;
        float zoomX = 1;
        float zoomY = 1;
        if (zoom != 1.0F) {
            posestack.translate((double) zoomX, (double) (-zoomY), 0.0D);
            posestack.scale(zoom, zoom, 1.0F);
        }

        posestack.last().pose().multiply(Matrix4f.perspective(pFov, (float) size / size, 0.05F, renderDistance * 4f));
        return posestack.last().pose();
    }

    public static void drawItem(FrameBufferBackedDynamicTexture tex, ItemStack stack) {

        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);
        //render to this one
        frameBuffer.bindWrite(true);

        int size = 16;
        //gui setup code
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
        Matrix4f matrix4f = Matrix4f.orthographic(0.0F,
                size, 0, size, 1000.0F, 3000); //ForgeHooksClient.getGuiFarPlane()
        RenderSystem.setProjectionMatrix(matrix4f);

        //model view stuff
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();

        posestack.translate(0.0D, 0.0D, 1000F - 3000); //ForgeHooksClient.getGuiFarPlane()
        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code

        //render stuff
        ItemRenderer itemRenderer = mc.getItemRenderer();
        //Minecraft.getInstance().gui.render(posestack,1);
        itemRenderer.renderGuiItem(stack, 0, 0);

        //reset stuff
        posestack.popPose();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.setProjectionMatrix(oldProjection);
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);
    }

    /*
        RenderSystem.setShaderTexture(0,
                new ResourceLocation("textures/gui/container/villager2.png")
       );
        Gui.blit(posestack,0,0,1000,0,0,
                256,256,16,16);
*/
}
