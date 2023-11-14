package net.mehvahdjukaar.moonlight.api.map.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class DecorationRenderer<T extends CustomMapDecoration> {
    protected final ResourceLocation textureId;
    protected final int mapColor;
    protected final boolean renderOnFrame;

    @Deprecated(forRemoval = true)
    public boolean rendersText = true;

    public DecorationRenderer(ResourceLocation texture, int mapColor, boolean renderOnFrame) {
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

    public int getAlpha(T de) {
        return 255;
    }

    public ResourceLocation getTextureId() {
        return textureId;
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
        if (!isOnFrame || renderOnFrame) {

            matrixStack.pushPose();
            matrixStack.translate(0.0F + (float) decoration.getX() / 2.0F + 64.0F, 0.0F + (float) decoration.getY() / 2.0F + 64.0F, -0.02F);
            matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (decoration.getRot() * 360) / 16.0F));
            matrixStack.scale(4.0F, 4.0F, 3.0F);
            if (CompatHandler.MAP_ATLASES) {
                MapAtlasCompat.scaleDecoration(matrixStack);
            }
            //matrixStack.translate(-0.125D, 0.125D, 0.0D);

            renderSprite(decoration, matrixStack, vertexBuilder, light, index);

            matrixStack.popPose();

            if (decoration.getDisplayName() != null && rendersText) {
                renderName(decoration, matrixStack, buffer, light);
            }
            return true;
        }
        return false;
    }

    // renders centered sprite
    public void renderSprite(T decoration, PoseStack matrixStack, VertexConsumer vertexBuilder, int light, int index) {
        int color = this.getColor(decoration);

        int b = FastColor.ARGB32.blue(color);
        int g = FastColor.ARGB32.green(color);
        int r = FastColor.ARGB32.red(color);

        TextureAtlasSprite sprite = MapDecorationClientManager.getAtlasSprite(this.getTextureId());
        //so we can use local coordinates
        //idk wy wrap doesnt work, it does the same as here
        //vertexBuilder = sprite.wrap(vertexBuilder);

        int alpha = this.getAlpha(decoration);
        if (alpha != 0) RenderUtil.renderSprite(matrixStack, vertexBuilder, light, index, b, g, r, alpha, sprite);
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
