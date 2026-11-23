package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

//This is a minor hack. use with care
public final class QuickPictureInPicture {

    @FunctionalInterface
    public interface Drawer {
        void submit(PoseStack poseStack, SubmitNodeCollector collector);
    }

    public static void register() {
        ClientHelper.addPictureInPictureRendererRegistration(event -> event.register(State.class, Renderer::new));
    }

    /**
     * Like submitCustomGeometry but for a gui. Draws 3d stuff in the given box without having to register a picture in picture state.
     * Origin is the center of the box, y goes up and 1 unit is scale pixels.
     * Unsafe, only 1 per frame. Be sure the stuff you pass in your drawer lambda is effectively immutable
     */
    public static void submit(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float scale,
                              Lighting.Entry lighting, Drawer drawer) {
        graphics.guiRenderState.addPicturesInPictureState(new State(drawer, lighting, x0, y0, x1, y1, scale,
                new Matrix3x2f(graphics.pose()), graphics.scissorStack.peek()));
    }

    public record State(Drawer drawer, Lighting.Entry lighting, int x0, int y0, int x1, int y1,
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
            return "moonlight quick pip";
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2f;
        }

        @Override
        protected void renderToTexture(State state, PoseStack poseStack) {
            Minecraft mc = Minecraft.getInstance();
            poseStack.scale(1, -1, -1);
            mc.gameRenderer.getLighting().setupFor(state.lighting());

            FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
            state.drawer().submit(poseStack, dispatcher.getSubmitNodeStorage());
            dispatcher.renderAllFeatures();
        }
    }
}
