package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class GuiHelper {

    // the tiling list background and footer shadow vanilla selection lists use. Those fields are private on
    // AbstractSelectionList, so they're mirrored here for our custom-scrolled screens
    private static final ResourceLocation MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    // Screen keeps the in-world menu background private, so mirror it here
    private static final ResourceLocation INWORLD_MENU_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    /** The top bar plus its bottom separator, no title. Same chrome as vanilla's header layouts, just taller. */
    public static void renderHeaderBar(GuiGraphics graphics, int width, int headerHeight) {
        boolean inWorld = Minecraft.getInstance().level != null;
        Screen.renderMenuBackgroundTexture(graphics, inWorld ? INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND,
                0, 0, 0f, 0f, width, headerHeight - 2);
        ResourceLocation separator = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(separator, 0, headerHeight - 2, 0f, 0f, width, 2, 32, 2);
        RenderSystem.disableBlend();
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
        // centered as one block within the bar, separator excluded
        int top = (headerHeight - 2 - (2 * font.lineHeight + gap)) / 2;
        graphics.drawCenteredString(font, title, width / 2, top, ConfigGuiColors.TITLE);
        graphics.drawCenteredString(font, subtitle, width / 2, top + font.lineHeight + gap, ConfigGuiColors.DESCRIPTION);
    }

    /**
     * A left to right gradient, which GuiGraphics.fillGradient can't do. Drawn as 1px columns, so keep the span
     * narrow (edge fades, highlights) instead of filling whole screens with it.
     */
    public static void fillGradientHorizontal(GuiGraphics graphics, int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        fillGradientHorizontal(graphics, RenderType.gui(), minX, minY, maxX, maxY, colorFrom, colorTo);
    }

    /**
     * As above, over a given render type. Pass RenderType.guiOverlay() to fade over rendered items: they write depth
     * at z 150, so the default RenderType.gui() is depth tested and gets punched out by them.
     */
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
        ResourceLocation bg = Minecraft.getInstance().level != null ? INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND;
        Screen.renderMenuBackgroundTexture(graphics, bg, x, y, x, y, width, height);
    }

    /** The 2px separator sprite of the header/footer bands, usable as a divider anywhere. */
    public static void renderSeparator(GuiGraphics graphics, int x, int y, int width) {
        ResourceLocation sprite = Minecraft.getInstance().level != null ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
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
    public static void renderModIcon(GuiGraphics graphics, ModIcons.Icon icon, int x, int y, int maxWidth, int maxHeight) {
        int h = maxHeight;
        int w = Math.round(maxHeight * (icon.width() / (float) icon.height()));
        if (w > maxWidth) {
            w = maxWidth;
            h = Math.round(maxWidth * (icon.height() / (float) icon.width()));
        }
        graphics.blit(icon.texture(), x + (maxWidth - w) / 2, y + (maxHeight - h) / 2, w, h,
                0f, 0f, icon.width(), icon.height(), icon.width(), icon.height());
    }

    /** The tiling list background over a scroll panel, the way AbstractSelectionList draws it. */
    public static void renderListBackground(GuiGraphics graphics, int top, int bottom, int width, double scroll) {
        ResourceLocation bg = Minecraft.getInstance().level != null ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        RenderSystem.enableBlend();
        graphics.blit(bg, 0, top, (float) width, (float) (bottom + (int) scroll), width, bottom - top, 32, 32);
        RenderSystem.disableBlend();
    }

    /** The bottom inner shadow strip, the way AbstractSelectionList draws it. */
    public static void renderFooterSeparator(GuiGraphics graphics, int bottom, int width) {
        ResourceLocation footer = Minecraft.getInstance().level != null ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(footer, 0, bottom, 0f, 0f, width, 2, 32, 2);
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
        graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFB0B0B0);
    }

    /**
     * The placeholder mod icon: a dark square with the mod's capital initial, or gearIcon when the name is blank.
     * Colors are passed in so callers can dim it.
     */
    public static void renderInitialTile(GuiGraphics graphics, Font font, String name, int x, int y, int size,
                                         int tileColor, int letterColor, ResourceLocation gearIcon) {
        graphics.fill(x, y, x + size, y + size, tileColor);
        graphics.renderOutline(x, y, size, size, 0xFF000000);
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
    public static void renderScrollingText(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            graphics.drawString(font, text, minX, textY, color); // fits: left aligned
        }
    }

    /** Like renderScrollingText, but centered while the text fits and only scrolling once it doesn't. */
    public static void renderScrollingTextCentered(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (!scrollIfOverflow(graphics, font, text, minX, maxX, rowTop, rowHeight, textY, color)) {
            int cx = minX + (maxX - minX - font.width(text)) / 2; // fits: centered
            graphics.drawString(font, text, cx, textY, color);
        }
    }

    // marquees the text if it overflows the band, returning false without drawing when it fits
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
