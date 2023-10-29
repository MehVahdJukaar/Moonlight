package net.mehvahdjukaar.moonlight.api.map.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class DecorationRenderer<T extends CustomMapDecoration> {
    private final RenderType RENDER_TYPE;
    private final int mapColor;
    private final boolean renderOnFrame;
    private  boolean rendersText = true;
    private final ResourceLocation textureId;

    public DecorationRenderer(ResourceLocation texture,  int mapColor, boolean renderOnFrame){
        this.RENDER_TYPE = RenderType.text(texture);
        this.renderOnFrame = renderOnFrame;
        this.mapColor = mapColor;
        this.textureId = texture;
    }

    public DecorationRenderer(ResourceLocation texture, int mapColor) {
        this(texture, mapColor, true);
    }

    public DecorationRenderer(ResourceLocation texture) {
        this(texture, -1, true);
    }

    public int getColor(T decoration) {
        return mapColor;
    }

    public RenderType getRenderType(T decoration) {
        return RENDER_TYPE;
    }

    public boolean render(T decoration, PoseStack matrixStack, MultiBufferSource buffer, MapItemSavedData mapData, boolean isOnFrame, int light, int index) {
        if (!isOnFrame || renderOnFrame) {

            matrixStack.pushPose();
            matrixStack.translate(0.0F + (float) decoration.getX() / 2.0F + 64.0F, 0.0F + (float) decoration.getY() / 2.0F + 64.0F, -0.02F);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) (decoration.getRot() * 360) / 16.0F));
            matrixStack.scale(4.0F, 4.0F, 3.0F);
            if (CompatHandler.MAP_ATLASES) {
                MapAtlasCompat.scaleDecoration(matrixStack);
            }
            //matrixStack.translate(-0.125D, 0.125D, 0.0D);

            Matrix4f matrix4f1 = matrixStack.last().pose();

            VertexConsumer vertexBuilder = buffer.getBuffer(this.getRenderType(decoration));

            int color = this.getColor(decoration);

            int b = NativeImage.getR(color);
            int g = NativeImage.getG(color);
            int r = NativeImage.getB(color);

            vertexBuilder.vertex(matrix4f1, -1.0F, 1.0F,  index * -0.001F).color(r, g, b, 255).uv(0, 1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, 1.0F,  index * -0.001F).color(r, g, b, 255).uv(1, 1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, -1.0F,  index * -0.001F).color(r, g, b, 255).uv(1, 0).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, -1.0F, -1.0F,  index * -0.001F).color(r, g, b, 255).uv(0, 0).uv2(light).endVertex();
            matrixStack.popPose();
            if (decoration.getDisplayName() != null && rendersText) {
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
                font.drawInBatch(displayName, 0.0F, 0.0F, -1, false, matrixStack.last().pose(), buffer, false, Integer.MIN_VALUE, light);

                matrixStack.popPose();
            }
            return true;
        }
        return false;
    }


}
