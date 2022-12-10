package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class TextUtil {

    private static final FormattedCharSequence CURSOR_MARKER = FormattedCharSequence.forward("_", Style.EMPTY);

    //IDK how this works anymore...

    /**
     * Scales and splits the given lines such that they fix in the given area with the maximum possible scale
     *
     * @param text   original text
     * @param width  box width
     * @param height box height
     * @return Pair of split lines and scale at which they should be rendered
     */
    public static Pair<List<FormattedCharSequence>, Float> fitLinesToBox(Font font, FormattedText text, float width, float height) {
        int scalingFactor;
        List<FormattedCharSequence> splitLines;
        int fontWidth = font.width(text);

        float maxLines;
        do {
            scalingFactor = Mth.floor(Mth.sqrt((fontWidth * 8f) / (width * height)));

            splitLines = font.split(text, Mth.floor(width * scalingFactor));
            //tempPageLines = RenderComponentsUtil.splitText(txt, MathHelper.floor(lx * scalingfactor), font, true, true);

            maxLines = height * scalingFactor / 8f;
            fontWidth += 1;
            // when lines fully filled @scaling factor > actual lines -> no overflow lines
        } while (maxLines < splitLines.size());

        return Pair.of(splitLines, 1f / scalingFactor);
    }

    //not server safe don't use
    @Deprecated(forRemoval = true)
    public static String getReadableName(String name) {
        return Arrays.stream((name).replace(":", "_").split("_"))
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static FormattedText parseText(String s) {
        try {
            FormattedText mutableComponent = Component.Serializer.fromJson(s);
            if (mutableComponent != null) {
                return mutableComponent;
            }
        } catch (Exception ignored) {
        }
        return FormattedText.of(s);
    }


    /**
     * Render a line in a GUI
     */
    public static void renderGuiLine(RenderTextProperties properties, String string, Font font, PoseStack poseStack,
                                     MultiBufferSource.BufferSource buffer,
                                     int cursorPos, int selectionPos, boolean isSelected, boolean blink, int yOffset) {
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();

        if (string != null) {
            if (font.isBidirectional()) {
                string = font.bidirectionalShaping(string);
            }
            //int centerX = (-font.width(string) / 2);

            FormattedCharSequence charSequence = FormattedCharSequence.forward(string, properties.style);
            float centerX = (-font.width(charSequence) / 2f);
            renderLineInternal(charSequence, font, centerX, yOffset, matrix4f, buffer, properties);

            String substring = string.substring(0, Math.min(cursorPos, string.length()));
            if (isSelected) {

                int pX = (int) (font.width(FormattedCharSequence.forward(substring, properties.style)) + centerX);

                if (blink) {
                    if (cursorPos >= string.length()) {
                        renderLineInternal(CURSOR_MARKER, font, pX, yOffset, matrix4f, buffer, properties);
                    }
                    buffer.endBatch();
                }

                //highlight
                if (blink && cursorPos < string.length()) {
                    GuiComponent.fill(poseStack, pX, yOffset - 1, pX + 1, yOffset + 9, -16777216 | properties.textColor);
                }

                if (selectionPos != cursorPos) {
                    int l3 = Math.min(cursorPos, selectionPos);
                    int l1 = Math.max(cursorPos, selectionPos);
                    int i2 = font.width(string.substring(0, l3)) - font.width(string) / 2;
                    int j2 = font.width(string.substring(0, l1)) - font.width(string) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(matrix4f, k2, (yOffset + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, l2, (yOffset + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, l2, yOffset, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, k2, yOffset, 0.0F).color(0, 0, 255, 255).endVertex();
                    BufferUploader.drawWithShader(bufferbuilder.end());
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
            if (!(isSelected && blink)) {
                buffer.endBatch();
            }
        }
    }

    /**
     * Renders multiple lines in a GUI
     */
    public static void renderGuiText(RenderTextProperties properties, String[] guiLines, Font font, PoseStack poseStack,
                                     MultiBufferSource.BufferSource buffer,
                                     int cursorPos, int selectionPos, int currentLine, boolean blink, int lineSpacing) {

        int nOfLines = guiLines.length;

        for (int line = 0; line < nOfLines; ++line) {
            int yOffset = line * lineSpacing - nOfLines * 5;
            renderGuiLine(properties, guiLines[line], font, poseStack, buffer, cursorPos, selectionPos,
                    line == currentLine, blink, yOffset);
        }
    }

    /**
     * Render text line in world
     */
    public static void renderLine(FormattedCharSequence formattedCharSequences, Font font, float yOffset, PoseStack poseStack,
                                  MultiBufferSource buffer, RenderTextProperties properties) {
        if (formattedCharSequences == null) return;
        float x = (-font.width(formattedCharSequences) / 2f);
        renderLineInternal(formattedCharSequences, font, x, yOffset, poseStack.last().pose(), buffer, properties);
    }

    /**
     * Renders multiple lines in world
     */
    public static void renderAllLines(FormattedCharSequence[] charSequences, int ySeparation, Font font, PoseStack poseStack,
                                      MultiBufferSource buffer, RenderTextProperties properties) {
        for (int i = 0; i < charSequences.length; i++) {
            renderLine(charSequences[i], font, ySeparation * (float) i, poseStack, buffer, properties);
        }
    }

    private static void renderLineInternal(FormattedCharSequence formattedCharSequences, Font font, float xOffset, float yOffset,
                                           Matrix4f matrix4f, MultiBufferSource buffer, RenderTextProperties properties) {
        if (properties.glowing) {
            font.drawInBatch8xOutline(formattedCharSequences, xOffset, yOffset, properties.textColor, properties.darkenedColor,
                    matrix4f, buffer, properties.light);
        } else {
            font.drawInBatch(formattedCharSequences, xOffset, yOffset, properties.darkenedColor, false,
                    matrix4f, buffer, false, 0, properties.light);
        }
    }


    private static int getDarkenedColor(int color, boolean glowing) {
        if (color == DyeColor.BLACK.getTextColor() && glowing) return 0xFFF0EBCC;
        return getDarkenedColor(color, 0.4f);
    }

    private static int getDarkenedColor(int color, float amount) {
        int j = (int) ((double) NativeImage.getR(color) * amount);
        int k = (int) ((double) NativeImage.getG(color) * amount);
        int l = (int) ((double) NativeImage.getB(color) * amount);
        return NativeImage.combine(0, l, k, j);
    }

    /**
     * bundles all data needed to render a generic text line. Useful for signs like blocks
     */
    public record RenderTextProperties(int textColor, int darkenedColor, boolean glowing, int light, Style style) {

        public RenderTextProperties(DyeColor color, boolean glowing, int combinedLight, Style style, BooleanSupplier isVeryNear) {
            this(color.getTextColor(),
                    getDarkenedColor(color.getTextColor(), glowing),
                    glowing && (isVeryNear.getAsBoolean() || color == DyeColor.BLACK),
                    glowing ? combinedLight : LightTexture.FULL_BRIGHT, style);

        }
    }

}
