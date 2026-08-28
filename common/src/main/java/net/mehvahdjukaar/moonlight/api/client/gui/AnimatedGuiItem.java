package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

/**
 * Draws an item in a GUI with a 3D transform and an optional tint. Each distinct item costs an offscreen
 * render target, so use it sparingly. Call register once during client setup.
 */
public final class AnimatedGuiItem {

    /**
     * Applied inside the item's own display transform, so a rotation spins the model about its own axis.
     * blockModel is true when the item renders as a 3D block instead of a flat sprite.
     */
    @FunctionalInterface
    public interface Transform {
        void apply(Matrix4f pose, boolean blockModel);
    }

    public static void register() {
        ClientHelper.addPictureInPictureRendererRegistration(event -> event.register(State.class, Renderer::new));
    }

    /** A tint of -1 leaves the colors alone. */
    public static void submit(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int size,
                              int tint, Transform transform) {
        if (stack.isEmpty()) return;
        // target is twice the icon so a spinning block doesn't clip on its diagonal
        int pad = size / 2;
        // neoforge adds submitPictureInPictureRenderState/peekScissorStack for this, vanilla has neither
        graphics.guiRenderState.addPicturesInPictureState(new State(stack, transform, tint,
                x - pad, y - pad, x + size + pad, y + size + pad, size,
                new Matrix3x2f(graphics.pose()), graphics.scissorStack.peek()));
    }

    public record State(ItemStack stack, Transform transform, int tint, int x0, int y0, int x1, int y1,
                        float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea)
            implements PictureInPictureRenderState {

        @Nullable
        @Override
        public ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
        }
    }

    public static class Renderer extends PictureInPictureRenderer<State> {

        public Renderer(MultiBufferSource.BufferSource bufferSource) {
            super(bufferSource);
        }

        @Override
        public Class<State> getRenderStateClass() {
            return State.class;
        }

        @Override
        protected String getTextureLabel() {
            return "moonlight animated item";
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2f; // the item sits in the middle of the target, not on its bottom edge
        }

        @Override
        protected void renderToTexture(State state, PoseStack poseStack) {
            Minecraft mc = Minecraft.getInstance();
            ItemStackRenderState itemState = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(itemState, state.stack(), ItemDisplayContext.GUI,
                    mc.level, mc.player, 0);
            boolean blockModel = itemState.usesBlockLight();

            Matrix4f local = new Matrix4f();
            state.transform().apply(local, blockModel);
            for (int i = 0; i < itemState.activeLayerCount; i++) {
                itemState.layers[i].localTransform.mul(local);
            }

            poseStack.scale(1, -1, -1);
            Lighting lighting = mc.gameRenderer.getLighting();
            lighting.setupFor(blockModel ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);

            FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
            itemState.submit(poseStack, dispatcher.getSubmitNodeStorage(), LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, 0);
            dispatcher.renderAllFeatures();
        }

        @Override
        protected void blitTexture(State state, GuiRenderState guiRenderState) {
            if (state.tint() == -1) {
                super.blitTexture(state, guiRenderState);
                return;
            }
            // same blit as super but tinted. premultiplied, so a grey tint darkens evenly
            guiRenderState.addBlitToCurrentLayer(new BlitRenderState(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                    TextureSetup.singleTexture(this.textureView, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                    state.pose(), state.x0(), state.y0(), state.x1(), state.y1(),
                    0f, 1f, 1f, 0f, state.tint(), state.scissorArea(), state.bounds()));
        }
    }
}
