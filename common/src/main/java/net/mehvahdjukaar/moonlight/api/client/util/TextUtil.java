package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.RegistryOps;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
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

    public static FormattedText parseText(String s, @Nullable HolderLookup.Provider provider) {
        if (provider != null) {
            try {
                RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
                return CodecUtils.FLAT_COMPONENT.parse(ops, JsonParser.parseString(s)).getOrThrow();
            } catch (Exception ignored) {
            }
        }
        return FormattedText.of(s);
    }

    /**
     * Render a line in a GUI
     */
    public static void renderGuiLine(RenderProperties properties, String string, Font font, GuiGraphicsExtractor graphics,
                                     int cursorPos, int selectionPos, boolean isSelected, boolean blink, int yOffset,
                                     int textLineHeight) {
        int textColor = properties.textColor;
        int twoTextLineHeight = 2 * textLineHeight;


        if (string != null) {
            int strWidth = font.width(string);
            int centerStr = strWidth - strWidth / 2;

            if (font.isBidirectional()) {
                string = font.bidirectionalShaping(string);
            }

            float centerX = -font.width(string) / 2f;
            graphics.text(font, string, (int) centerX, yOffset, textColor, false);

            if (isSelected) {
                if (blink) {
                    if (cursorPos >= string.length()) {
                        graphics.text(font, "_", centerStr, yOffset, textColor, false);
                    }
                }


                //highlight
                if (blink && cursorPos < string.length()) {
                    graphics.fill(centerStr, yOffset - 1, centerStr + 1, yOffset + textLineHeight, 0xFF000000 | textColor);
                }

                if (selectionPos != cursorPos) {
                    int minC = Math.min(cursorPos, selectionPos);
                    int maxC = Math.max(cursorPos, selectionPos);
                    int s = font.width(string.substring(0, minC)) - strWidth / 2;
                    int t = font.width(string.substring(0, maxC)) - strWidth / 2;
                    int startX = Math.min(s, t);
                    int v = Math.max(s, t);
                    graphics.textHighlight(startX, yOffset, v, yOffset + textLineHeight, false);
                }
            }
        }
    }

    /**
     * Renders multiple lines in a GUI
     */
    public static void renderGuiText(RenderProperties properties, String[] guiLines, Font font, GuiGraphicsExtractor graphics,
                                     int cursorPos, int selectionPos, int currentLine, boolean blink, int lineSpacing) {

        int nOfLines = guiLines.length;

        for (int line = 0; line < nOfLines; ++line) {
            int yOffset = line * lineSpacing - nOfLines * 5;
            renderGuiLine(properties, guiLines[line], font, graphics, cursorPos, selectionPos,
                    line == currentLine, blink, yOffset, lineSpacing);
        }
    }

    /**
     * Render text line in world
     */
    public static void renderLine(FormattedCharSequence formattedCharSequences, Font font, float yOffset, PoseStack poseStack,
                                  MultiBufferSource buffer, RenderProperties properties) {
        if (formattedCharSequences == null) return;
        float x = -font.width(formattedCharSequences) / 2f;
        Matrix4f matrix4f = poseStack.last().pose();
        if (properties.outline) {
            font.drawInBatch8xOutline(formattedCharSequences, x, yOffset, properties.textColor, properties.darkenedColor,
                    matrix4f, buffer, properties.light);
        } else {
            font.drawInBatch(formattedCharSequences, x, yOffset, properties.darkenedColor, false,
                    matrix4f, buffer, Font.DisplayMode.NORMAL, 0, properties.light);
        }
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
        return new RenderProperties(color, dark, outline, glowing ? LightCoordsUtil.FULL_BRIGHT : combinedLight, style);
    }


}
