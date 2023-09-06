package net.mehvahdjukaar.moonlight.api.map.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

public class DecorationRenderer<T extends CustomMapDecoration> {
    protected final ResourceLocation textureId;
    protected final int mapColor;
    protected final boolean renderOnFrame;

    public DecorationRenderer(ResourceLocation texture,  int mapColor, boolean renderOnFrame){
        this.renderOnFrame = renderOnFrame;
        this.mapColor = mapColor;
        this.textureId = texture;
        }

    public DecorationRenderer(ResourceLocation texture, int mapColor){
        this(texture,mapColor,true);
    }

    public DecorationRenderer(ResourceLocation texture){
        this(texture,-1,true);
    }

    public int getColor(T decoration) {
        return mapColor;
    }

    public boolean render(T decoration, PoseStack matrixStack, VertexConsumer vertexBuilder,
                          MultiBufferSource buffer, MapItemSavedData mapData, boolean isOnFrame, int light, int index) {
        if (!isOnFrame || renderOnFrame) {

            matrixStack.pushPose();
            matrixStack.translate(0.0F + (float) decoration.getX() / 2.0F + 64.0F, 0.0F + (float) decoration.getY() / 2.0F + 64.0F, -0.02F);
            matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (decoration.getRot() * 360) / 16.0F));
            matrixStack.scale(4.0F, 4.0F, 3.0F);
            if(MapHelper.MAP_ATLASES){
                MapAtlasCompat.scaleDecoration(matrixStack);
            }
            matrixStack.translate(-0.125D, 0.125D, 0.0D);

            Matrix4f matrix4f1 = matrixStack.last().pose();


            int color = this.getColor(decoration);

            int b = FastColor.ARGB32.blue(color);
            int g = FastColor.ARGB32.green(color);
            int r = FastColor.ARGB32.red(color);

            TextureAtlasSprite sprite = MapDecorationClientManager.getAtlasSprite(this.textureId);
            //so we can use local coordinates
            //idk wy wrap doesnt work, it does the same as here
            //vertexBuilder = sprite.wrap(vertexBuilder);
            float u0 = sprite.getU(0);
            float u1 = sprite.getU(16);
            float v0 = sprite.getV(0);
            float v1 = sprite.getV(16);
            vertexBuilder.vertex(matrix4f1, -1.0F, 1.0F, index * -0.001F).color(r, g, b, 255).uv(u0, v1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, 1.0F, index * -0.001F).color(r, g, b, 255).uv(u1, v1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, -1.0F, index * -0.001F).color(r, g, b, 255).uv(u1, v0).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, -1.0F, -1.0F, index * -0.001F).color(r, g, b, 255).uv(u0, v0).uv2(light).endVertex();

            matrixStack.popPose();
            if (decoration.getDisplayName() != null) {
                Font font = Minecraft.getInstance().font;
                Component displayName = decoration.getDisplayName();
                float width =  font.width(displayName);
                float clamped = Mth.clamp(25.0F / width, 0.0F, 6.0F / 9.0F);
                matrixStack.pushPose();
                matrixStack.translate( (0.0F + (float) decoration.getX() / 2.0F + 64.0F - width * clamped / 2.0F),  (0.0F + (float) decoration.getY() / 2.0F + 64.0F + 4.0F), (double) -0.025F);
                matrixStack.scale(clamped, clamped, 1.0F);
                matrixStack.translate(0.0D, 0.0D,  -0.1F);
                font.drawInBatch(displayName, 0.0F, 0.0F, -1, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, light);
                matrixStack.popPose();
            }
            return true;
        }
        return false;
    }
}
