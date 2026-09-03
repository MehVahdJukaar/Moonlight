package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

//General utility to render gui stuff.
public final class GuiHelper {

    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final Identifier INWORLD_MENU_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    /** The top bar plus its bottom separator, no title. Same chrome as vanilla's header layouts, just taller. */
    public static void renderHeaderBar(GuiGraphicsExtractor graphics, int width, int headerHeight) {
        boolean inWorld = Minecraft.getInstance().level != null;
        Screen.extractMenuBackgroundTexture(graphics, inWorld ? INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND,
                0, 0, 0f, 0f, width, headerHeight - 2);
        Identifier separator = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, separator, 0, headerHeight - 2, 0f, 0f, width, 2, 32, 2);
    }

    public static void renderHeaderBar(GuiGraphicsExtractor graphics, Font font, Component title, int width, int headerHeight) {
        renderHeaderBar(graphics, width, headerHeight);
        graphics.centeredText(font, title, width / 2, (headerHeight - font.lineHeight) / 2, ConfigGuiColors.TITLE);
    }

    /** The header bar with a gold title and a gray second line under it. */
    public static void renderHeaderBar(GuiGraphicsExtractor graphics, Font font, Component title, @Nullable Component subtitle,
                                       int width, int headerHeight) {
        if (subtitle == null) {
            renderHeaderBar(graphics, font, title, width, headerHeight);
            return;
        }
        renderHeaderBar(graphics, width, headerHeight);
        int gap = 2;
        int top = (headerHeight - 2 - (2 * font.lineHeight + gap)) / 2;
        graphics.centeredText(font, title, width / 2, top, ConfigGuiColors.TITLE);
        graphics.centeredText(font, subtitle, width / 2, top + font.lineHeight + gap, ConfigGuiColors.DESCRIPTION);
    }

    public static void fillGradientHorizontal(GuiGraphics graphics, int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        fillGradientHorizontal(graphics, RenderType.gui(), minX, minY, maxX, maxY, colorFrom, colorTo);
    }

    public static void fillGradientHorizontal(GuiGraphics graphics, RenderType renderType, int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        int steps = maxX - minX;
        if (steps <= 0) return;
        for (int i = 0; i < steps; i++) {
            int color = ARGB.srgbLerp(steps == 1 ? 0f : i / (float) (steps - 1), colorFrom, colorTo);
            graphics.fill(minX + i, minY, minX + i + 1, maxY, color);
        }
    }

    /** The plain menu background the header bar uses, over an arbitrary rect, tiling still aligned to the screen. */
    public static void renderMenuBand(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        Identifier bg = Minecraft.getInstance().level != null ? INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND;
        Screen.extractMenuBackgroundTexture(graphics, bg, x, y, x, y, width, height);
    }

    public static void renderSeparator(GuiGraphicsExtractor graphics, int x, int y, int width) {
        Identifier sprite = Minecraft.getInstance().level != null ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        graphics.blit(RenderPipelines.GUI_TEXTURED, sprite, x, y, 0f, 0f, width, 2, 32, 2);
    }

    /** Same as renderSeparator but vertical. Vanilla's sprites are horizontal, so this one is drawn by hand. */
    public static void renderVerticalSeparator(GuiGraphicsExtractor graphics, int x, int top, int bottom) {
        graphics.fill(x, top, x + 1, bottom, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.fill(x + 1, top, x + 2, bottom, 0x18FFFFFF);
    }

    /** A mod icon at its real aspect ratio, scaled to fit and centered inside the given box. */
    public static void renderModIcon(GuiGraphicsExtractor graphics, ModIcons.Icon icon, int x, int y, int maxWidth, int maxHeight) {
        int h = maxHeight;
        int w = Math.round(maxHeight * (icon.width() / (float) icon.height()));
        if (w > maxWidth) {
            w = maxWidth;
            h = Math.round(maxWidth * (icon.height() / (float) icon.width()));
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(),
                x + (maxWidth - w) / 2, y + (maxHeight - h) / 2, 0f, 0f, w, h,
                icon.width(), icon.height(), icon.width(), icon.height());
    }

    public static void renderListBackground(GuiGraphics graphics, int top, int bottom, int width, double scroll) {
        Identifier bg = Minecraft.getInstance().level != null ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        RenderSystem.enableBlend();
        graphics.blit(bg, 0, top, (float) width, (float) (bottom + (int) scroll), width, bottom - top, 32, 32);
        RenderSystem.disableBlend();
    }

    public static void renderFooterSeparator(GuiGraphics graphics, int bottom, int width) {
        Identifier footer = Minecraft.getInstance().level != null ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(footer, 0, bottom, 0f, 0f, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    /** Thin right-edge scrollbar for a custom-scrolled panel. No-op when everything fits. */
    public static void renderScrollbar(GuiGraphicsExtractor graphics, int top, int bottom, int width, double scroll, int maxScroll) {
        if (maxScroll <= 0) return;
        int trackX = width - 6;
        int trackH = bottom - top;
        int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
        int thumbY = top + (int) ((trackH - thumbH) * (scroll / maxScroll));
        graphics.fill(trackX, top, trackX + 3, top + trackH, 0x40000000);
        graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFB0B0B0);
    }

    /**
     * The placeholder mod icon: a dark square with the mod's capital initial, or gearIcon when the name is blank.
     * Colors are passed in so callers can dim it.
     */
    public static void renderInitialTile(GuiGraphicsExtractor graphics, Font font, String name, int x, int y, int size,
                                         int tileColor, int letterColor, Identifier gearIcon) {
        graphics.fill(x, y, x + size, y + size, tileColor);
        graphics.outline(x, y, size, size, 0xFF000000);
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            int g = size >= 26 ? 16 : 8; // the gear is 16x16, so only whole steps of it stay on the pixel grid
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, gearIcon, x + (size - g) / 2, y + (size - g) / 2, g, g);
            return;
        }
        String initial = trimmed.substring(0, 1).toUpperCase();
        int tx = x + (size - font.width(initial)) / 2;
        int ty = y + (size - font.lineHeight) / 2;
        graphics.text(font, initial, tx, ty, letterColor, false);
    }

    /** The vanilla button click sound, for clickable things that aren't widgets and can't play it themselves. */
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

    /**
     * Left aligned text that scrolls back and forth when it doesn't fit its box, like vanilla's
     * AbstractWidget.renderScrollingString, which we can't call.
     */
    public static void renderScrollingText(GuiGraphicsExtractor graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            graphics.text(font, text, minX, textY, color); // fits: left aligned
        }
    }

    /** Like renderScrollingText, but centered while the text fits and only scrolling once it doesn't. */
    public static void renderScrollingTextCentered(GuiGraphicsExtractor graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            int cx = minX + (maxX - minX - font.width(text)) / 2; // fits: centered
            graphics.text(font, text, cx, textY, color);
        }
    }

    private static boolean scrollIfOverflow(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int textY, int color) {
        int overflow = font.width(text) - (maxX - minX);
        if (overflow <= 0) return false;
        double seconds = (double) Util.getMillis() / 1000.0;
        double period = Math.max(overflow * 0.5, 3.0);
        double phase = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * seconds / period)) / 2.0 + 0.5;
        double offset = Mth.lerp(phase, 0.0, overflow);
        graphics.enableScissor(minX, rowTop, maxX, rowTop + rowHeight);
        graphics.text(font, text, minX - (int) offset, textY, color);
        graphics.disableScissor();
        return true;
    }
}
