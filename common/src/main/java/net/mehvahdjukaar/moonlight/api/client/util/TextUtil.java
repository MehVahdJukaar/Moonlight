package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BooleanSupplier;

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

    public static FormattedText parseText(String s) {
        try {
            FormattedText mutableComponent = Component.Serializer.fromJson(s, Utils.hackyGetRegistryAccess());
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
    public static void renderGuiLine(RenderProperties properties, String string, Font font, GuiGraphics graphics,
                                     MultiBufferSource.BufferSource buffer,
                                     int cursorPos, int selectionPos, boolean isSelected, boolean blink, int yOffset) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();

        if (string != null) {
            if (font.isBidirectional()) {
                string = font.bidirectionalShaping(string);
            }
            //int centerX = (-font.width(string) / 2);

            FormattedCharSequence charSequence = FormattedCharSequence.forward(string, properties.style);
            float centerX = -font.width(charSequence) / 2f;
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
                    graphics.fill(pX, yOffset - 1, pX + 1, yOffset + 9, -16777216 | properties.textColor);
                }

                if (selectionPos != cursorPos) {
                    int l3 = Math.min(cursorPos, selectionPos);
                    int l1 = Math.max(cursorPos, selectionPos);
                    int i2 = font.width(string.substring(0, l3)) - font.width(string) / 2;
                    int j2 = font.width(string.substring(0, l1)) - font.width(string) / 2;
                    int startX = Math.min(i2, j2);
                    int startY = Math.max(i2, j2);


                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    graphics.fill(startX, startY, yOffset, (yOffset + 9), -16776961);
                    RenderSystem.disableColorLogicOp();

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
    public static void renderGuiText(RenderProperties properties, String[] guiLines, Font font, GuiGraphics graphics,
                                     MultiBufferSource.BufferSource buffer,
                                     int cursorPos, int selectionPos, int currentLine, boolean blink, int lineSpacing) {

        int nOfLines = guiLines.length;

        for (int line = 0; line < nOfLines; ++line) {
            int yOffset = line * lineSpacing - nOfLines * 5;
            renderGuiLine(properties, guiLines[line], font, graphics, buffer, cursorPos, selectionPos,
                    line == currentLine, blink, yOffset);
        }
    }

    /**
     * Render text line in world
     */
    public static void renderLine(FormattedCharSequence formattedCharSequences, Font font, float yOffset, PoseStack poseStack,
                                  MultiBufferSource buffer, RenderProperties properties) {
        if (formattedCharSequences == null) return;
        float x = -font.width(formattedCharSequences) / 2f;
        renderLineInternal(formattedCharSequences, font, x, yOffset, poseStack.last().pose(), buffer, properties);
    }

    /**
     * Renders multiple lines in world
     */
    public static void renderAllLines(FormattedCharSequence[] charSequences, int ySeparation, Font font, PoseStack poseStack,
                                      MultiBufferSource buffer, RenderProperties properties) {
        for (int i = 0; i < charSequences.length; i++) {
            renderLine(charSequences[i], font, ySeparation * i, poseStack, buffer, properties);
        }
    }

    private static void renderLineInternal(FormattedCharSequence formattedCharSequences, Font font, float xOffset, float yOffset,
                                           Matrix4f matrix4f, MultiBufferSource buffer, RenderProperties properties) {
        if (properties.outline) {
            font.drawInBatch8xOutline(formattedCharSequences, xOffset, yOffset, properties.textColor, properties.darkenedColor,
                    matrix4f, buffer, properties.light);
        } else {
            font.drawInBatch(formattedCharSequences, xOffset, yOffset, properties.darkenedColor, false,
                    matrix4f, buffer, Font.DisplayMode.NORMAL, 0, properties.light);
        }
    }


    private static int getDarkenedColor(int color, boolean glowing, float mult) {
        if (color == DyeColor.BLACK.getTextColor() && glowing) return 0xFFF0EBCC;
        return ColorUtils.multiply(color, 0.4f * (glowing ? 1 : mult));
    }

    private static int getDarkenedColor(int color, boolean glowing) {
        return getDarkenedColor(color, glowing, 1);
    }

    //TODO: account for light. text doesnt account for light direction

    /**
     * bundles all data needed to render a generic text line. Useful for signs like blocks
     */
    public record RenderProperties(int textColor, int darkenedColor, boolean outline, int light, Style style) {
    }

    public static RenderProperties renderProperties(DyeColor dyeColor, boolean glowing,
                                                    int combinedLight, Style style, Vector3f normal, BooleanSupplier isVeryNear) {
        return renderProperties(dyeColor, glowing, 1, combinedLight, style, normal, isVeryNear);
    }

    public static RenderProperties renderProperties(DyeColor dyeColor, boolean glowing, float darkColorMult,
                                                    int combinedLight, Style style, Vector3f normal, BooleanSupplier isVeryNear) {
        boolean outline = glowing && (dyeColor == DyeColor.BLACK || isVeryNear.getAsBoolean());

        int textColor = dyeColor.getTextColor();
        float shading = ColorUtils.getShading(normal);
        int color = glowing ? textColor : ColorUtils.multiply(textColor, shading);
        int dark;
        if (!glowing || outline) {
            dark = getDarkenedColor(textColor, glowing, darkColorMult * shading);
        } else {
            dark = color;
        }
        return new RenderProperties(color, dark, outline, glowing ? LightTexture.FULL_BRIGHT : combinedLight, style);
    }


}
