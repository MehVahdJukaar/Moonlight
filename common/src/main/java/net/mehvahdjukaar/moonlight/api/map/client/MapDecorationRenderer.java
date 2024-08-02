package net.mehvahdjukaar.moonlight.api.map.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class MapDecorationRenderer<T extends MLMapDecoration> {
    protected final ResourceLocation textureId;

    public MapDecorationRenderer(ResourceLocation texture) {
        this.textureId = texture;
    }

    protected int getColor(T decoration) {
        return -1;
    }

    protected int getAlpha(T decoration) {
        return 255;
    }

    protected boolean hasOutline(T decoration) {
        return false;
    }

    protected boolean rendersOnFrame(T decoration) {
        return true;
    }

    public boolean render(T decoration, PoseStack matrixStack, VertexConsumer vertexBuilder,
                          MultiBufferSource buffer,
                          @Nullable MapItemSavedData mapData,
                          boolean isOnFrame, int light, int index) {
        return render(decoration, matrixStack, vertexBuilder, buffer, mapData, isOnFrame, light, index, true);
    }

    public boolean render(T decoration, PoseStack matrixStack, VertexConsumer vertexBuilder,
                          MultiBufferSource buffer,
                          @Nullable MapItemSavedData mapData,
                          boolean isOnFrame, int light, int index, boolean rendersText) {
        if (!isOnFrame || rendersOnFrame(decoration)) {

            matrixStack.pushPose();
            matrixStack.translate(0.0F + (float) decoration.getX() / 2.0F + 64.0F, 0.0F + (float) decoration.getY() / 2.0F + 64.0F, -0.02F);
            matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (decoration.getRot() * 360) / 16.0F));
            matrixStack.scale(4.0F, 4.0F, 3.0F);
            if (CompatHandler.MAP_ATLASES) {
                MapAtlasCompat.scaleDecoration(matrixStack);
            }
            //matrixStack.translate(-0.125D, 0.125D, 0.0D);

            renderDecorationSprite(matrixStack, buffer, vertexBuilder, light, index,
                    this.getColor(decoration), this.getAlpha(decoration), this.hasOutline(decoration));

            matrixStack.popPose();

            if (decoration.getDisplayName() != null && rendersText) {
                renderName(decoration, matrixStack, buffer, light);
            }
            return true;
        }
        return false;
    }


    // renders centered sprite
    public void renderDecorationSprite(PoseStack matrixStack, MultiBufferSource buffer, VertexConsumer vertexBuilder, int light, int index,
                                       int color, int alpha, boolean outline) {

        int b = FastColor.ARGB32.blue(color);
        int g = FastColor.ARGB32.green(color);
        int r = FastColor.ARGB32.red(color);

        RenderSystem.enableDepthTest();
        TextureAtlasSprite sprite = Minecraft.getInstance().getMapDecorationTextures().getSprite(textureId);
        //so we can use local coordinates
        //idk wy wrap doesnt work, it does the same as here
        //vertexBuilder = sprite.wrap(vertexBuilder);

        if (alpha != 0) {
            RenderUtil.renderSprite(matrixStack, vertexBuilder, light, index, b, g, r, alpha, sprite);

            if (outline) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                VertexConsumer vb2 = buffer.getBuffer(RenderUtil.getTextColorRenderType(MapDecorationClientManager.LOCATION_MAP_MARKERS));
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        if (j != 0 || k != 0) {
                            matrixStack.pushPose();
                            matrixStack.translate(j * 0.125, k * 0.125, 0.001);
                            RenderUtil.renderSprite(matrixStack, vb2, LightTexture.FULL_BRIGHT, index, 255, 255, 255, alpha, sprite);
                            matrixStack.popPose();
                        }
                    }
                }
            }
        }
    }


    protected void renderName(T decoration, PoseStack matrixStack, MultiBufferSource buffer, int light) {
        Font font = Minecraft.getInstance().font;
        Component displayName = decoration.getDisplayName();
        float width = font.width(displayName);
        float scale = Mth.clamp(25.0F / width, 0.0F, 6.0F / 9.0F);
        matrixStack.pushPose();
        matrixStack.translate((0.0F + (float) decoration.getX() / 2.0F + 64.0F - width * scale / 2.0F), (0.0F + (float) decoration.getY() / 2.0F + 64.0F + 4.0F), (double) -0.025F);
        if (CompatHandler.MAP_ATLASES) {
            MapAtlasCompat.scaleDecorationText(matrixStack, width, scale);
        }
        matrixStack.scale(scale, scale, 1.0F);
        matrixStack.translate(0.0D, 0.0D, -0.1F);
        font.drawInBatch(displayName, 0.0F, 0.0F, -1, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, light);
        matrixStack.popPose();
    }


}
