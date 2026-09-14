package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

//General utility to render gui stuff.
public final class GuiHelper {

    private static boolean inWorld() {
        return Minecraft.getInstance().level != null;
    }

    private static ResourceLocation menuBackground() {
        return inWorld() ? Screen.INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND;
    }

    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** The top bar plus its bottom separator, no title. Same chrome as vanilla's header layouts, just taller. */
    public static void renderHeaderBar(GuiGraphics graphics, int width, int headerHeight) {
        Screen.renderMenuBackgroundTexture(graphics, menuBackground(), 0, 0, 0f, 0f, width, headerHeight - 2);
        renderSeparator(graphics, 0, headerHeight - 2, width);
    }

    /** The header bar with the gold screen title centered in it. */
    public static void renderHeaderBar(GuiGraphics graphics, Font font, Component title, int width, int headerHeight) {
        renderHeaderBar(graphics, width, headerHeight);
        graphics.drawCenteredString(font, title, width / 2, (headerHeight - font.lineHeight) / 2, ConfigGuiColors.TITLE);
    }

    /** The header bar with a gold title and a gray second line under it. */
    public static void renderHeaderBar(GuiGraphics graphics, Font font, Component title, @Nullable Component subtitle,
                                       int width, int headerHeight) {
        if (subtitle == null) {
            renderHeaderBar(graphics, font, title, width, headerHeight);
            return;
        }
        renderHeaderBar(graphics, width, headerHeight);
        int gap = 2;
        int top = (headerHeight - 2 - (2 * font.lineHeight + gap)) / 2;
        graphics.drawCenteredString(font, title, width / 2, top, ConfigGuiColors.TITLE);
        graphics.drawCenteredString(font, subtitle, width / 2, top + font.lineHeight + gap, ConfigGuiColors.DESCRIPTION);
    }

    public static void fillGradientHorizontal(GuiGraphics graphics, int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        fillGradientHorizontal(graphics, RenderType.gui(), minX, minY, maxX, maxY, colorFrom, colorTo);
    }

    public static void fillGradientHorizontal(GuiGraphics graphics, RenderType renderType, int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        int steps = maxX - minX;
        if (steps <= 0) return;
        for (int i = 0; i < steps; i++) {
            int color = FastColor.ARGB32.lerp(steps == 1 ? 0f : i / (float) (steps - 1), colorFrom, colorTo);
            graphics.fill(renderType, minX + i, minY, minX + i + 1, maxY, color);
        }
    }

    /** The plain menu background the header bar uses, over an arbitrary rect, tiling still aligned to the screen. */
    public static void renderMenuBand(GuiGraphics graphics, int x, int y, int width, int height) {
        Screen.renderMenuBackgroundTexture(graphics, menuBackground(), x, y, x, y, width, height);
    }

    public static void renderSeparator(GuiGraphics graphics, int x, int y, int width) {
        ResourceLocation sprite = inWorld() ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(sprite, x, y, 0f, 0f, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    public static void renderFooterSeparator(GuiGraphics graphics, int x, int y, int width) {
        ResourceLocation sprite = inWorld() ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(sprite, x, y, 0f, 0f, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    /** Same as renderSeparator but vertical. Vanilla's sprites are horizontal, so this one is drawn by hand. */
    public static void renderVerticalSeparator(GuiGraphics graphics, int x, int top, int bottom) {
        graphics.fill(x, top, x + 1, bottom, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.fill(x + 1, top, x + 2, bottom, 0x18FFFFFF);
    }

    /** A mod icon at its real aspect ratio, scaled to fit and centered inside the given box. */
    public static void renderModIcon(GuiGraphics graphics, Icon icon, int x, int y, int maxWidth, int maxHeight) {
        int h = maxHeight;
        int w = Math.round(maxHeight * (icon.width() / (float) icon.height()));
        if (w > maxWidth) {
            w = maxWidth;
            h = Math.round(maxWidth * (icon.height() / (float) icon.width()));
        }
        graphics.blit(icon.texture(), x + (maxWidth - w) / 2, y + (maxHeight - h) / 2, w, h,
                0f, 0f, icon.width(), icon.height(), icon.width(), icon.height());
    }

    public static void renderListBackground(GuiGraphics graphics, int top, int bottom, int width, double scroll) {
        ResourceLocation bg = inWorld() ? AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND : AbstractSelectionList.MENU_LIST_BACKGROUND;
        RenderSystem.enableBlend();
        graphics.blit(bg, 0, top, (float) width, (float) (bottom + (int) scroll), width, bottom - top, 32, 32);
        RenderSystem.disableBlend();
    }

    /** Thin right-edge scrollbar for a custom-scrolled panel. No-op when everything fits. */
    public static void renderScrollbar(GuiGraphics graphics, int top, int bottom, int width, double scroll, int maxScroll) {
        if (maxScroll <= 0) return;
        int trackX = width - 6;
        int trackH = bottom - top;
        int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
        int thumbY = top + (int) ((trackH - thumbH) * (scroll / maxScroll));
        graphics.fill(trackX, top, trackX + 3, top + trackH, 0x40000000);
        graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, ConfigGuiColors.SCROLLBAR_THUMB);
    }

    /**
     * The placeholder mod icon: a dark square with the mod's capital initial, or gearIcon when the name is blank.
     * Colors are passed in so callers can dim it.
     */
    public static void renderInitialTile(GuiGraphics graphics, Font font, String name, int x, int y, int size,
                                         int tileColor, int letterColor, ResourceLocation gearIcon) {
        graphics.fill(x, y, x + size, y + size, tileColor);
        graphics.renderOutline(x, y, size, size, CommonColors.BLACK);
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            int g = size >= 26 ? 16 : 8; // the gear is 16x16, so only whole steps of it stay on the pixel grid
            graphics.blitSprite(gearIcon, x + (size - g) / 2, y + (size - g) / 2, g, g);
            return;
        }
        String initial = trimmed.substring(0, 1).toUpperCase();
        int tx = x + (size - font.width(initial)) / 2;
        int ty = y + (size - font.lineHeight) / 2;
        graphics.drawString(font, initial, tx, ty, letterColor, false);
    }

    public static Locale currentLocale() {
        String code = Minecraft.getInstance().getLanguageManager().getSelected();
        return Locale.forLanguageTag(code.replace('_', '-'));
    }

    // centers the visible glyphs in the box. Font#width counts the trailing spacing column and lineHeight the
    // descender row, so centering on those lands a pixel off
    public static void renderTextCenteredIn(GuiGraphics graphics, Font font, String text, int x, int y, int w, int h, int color) {
        int glyphW = font.width(text) - 1;
        graphics.drawString(font, text, x + (w - glyphW) / 2, y + (h - 7) / 2, color, false);
    }
    //for diagonals
    public static void renderChecker(GuiGraphics graphics, int x, int y, int w, int h) {
        int cell = 4;
        for (int yy = 0; yy < h; yy += cell) {
            for (int xx = 0; xx < w; xx += cell) {
                boolean light = (((xx / cell) + (yy / cell)) & 1) == 0;
                graphics.fill(x + xx, y + yy, Math.min(x + xx + cell, x + w), Math.min(y + yy + cell, y + h),
                        light ? ConfigGuiColors.CHECKER_LIGHT : ConfigGuiColors.CHECKER_DARK);
            }
        }
    }

    public static void renderLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) return;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    /**
     * @deprecated moved to TextHelper
     */
    @Deprecated(forRemoval = true)
    public static String formatNumber(double v) {
        return TextHelper.formatNumber(v);
    }

    public static void renderScrollingText(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            graphics.drawString(font, text, minX, textY, color); // fits: left aligned
        }
    }

    public static void renderScrollingTextCentered(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            int cx = minX + (maxX - minX - font.width(text)) / 2; // fits: centered
            graphics.drawString(font, text, cx, textY, color);
        }
    }

    public static void renderClippedText(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int y, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + font.lineHeight + 1);
        graphics.drawString(font, text, minX, y, color);
        graphics.disableScissor();
    }

    public static void renderClippedTextCentered(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int y, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + font.lineHeight + 1);
        graphics.drawCenteredString(font, text, (minX + maxX) / 2, y, color);
        graphics.disableScissor();
    }

    private static boolean scrollIfOverflow(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int textY, int color) {
        int overflow = font.width(text) - (maxX - minX);
        if (overflow <= 0) return false;
        double seconds = (double) Util.getMillis() / 1000.0;
        double period = Math.max(overflow * 0.5, 3.0);
        double phase = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * seconds / period)) / 2.0 + 0.5;
        double offset = Mth.lerp(phase, 0.0, overflow);
        graphics.enableScissor(minX, rowTop, maxX, rowTop + rowHeight);
        graphics.drawString(font, text, minX - (int) offset, textY, color);
        graphics.disableScissor();
        return true;
    }
}
