package net.mehvahdjukaar.moonlight.api.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * A hub listing every mod that registered a config with Moonlight, drawn as a scrollable grid of cards (mod icon +
 * name + version). Clicking a card opens that mod's config(s) (via {@link MoonlightConfigSelectScreen#create}).
 * Reached from the bottom-left button on the config screens.
 */
public class ModsTilesScreen extends Screen {

    private static final ResourceLocation GEAR_ICON = Moonlight.res("config");
    // the same tiling list background the vanilla selection lists use (the field is private on AbstractSelectionList)
    private static final ResourceLocation MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private static final int GRID_PAD = 8; // inset for the first/last card row inside the scroll panel

    private static final int CARD_W = 88;
    private static final int CARD_PAD = 9;        // equal padding above the icon and below the last text line
    private static final int ICON_TEXT_GAP = 6;   // icon → name
    private static final int NAME_VER_GAP = 2;    // name → version
    private static final int ICON_SIZE = 32;
    private static final int LINE = 9;            // vanilla font line height
    // top pad + icon + gap + name + gap + version + bottom pad — kept balanced (CARD_PAD on both ends)
    private static final int CARD_H = CARD_PAD + ICON_SIZE + ICON_TEXT_GAP + LINE + NAME_VER_GAP + LINE + CARD_PAD;
    private static final int CARD_GAP = 6;
    private static final int SIDE_MARGIN = 24;

    private static final int CARD_BG = 0xFF1B1B20;
    private static final int CARD_BG_HOVER = 0xFF2C2C34;
    private static final int CARD_OUTLINE = 0xFF000000;
    private static final int CARD_OUTLINE_HOVER = 0xFF000000 | CATEGORY_COLOR; // aqua accent (opaque)
    private static final int VERSION_COLOR = DESCRIPTION_COLOR;

    private final Screen parent;
    @Nullable
    private final ResourceLocation background;
    private final List<Entry> entries = new ArrayList<>();

    private double scroll;
    private int maxScroll;
    // recomputed each layout pass, shared by render + click
    private int cols, startX, contentTop, contentBottom;

    public ModsTilesScreen(Screen parent, @Nullable ResourceLocation background) {
        super(Component.translatable("gui.moonlight.config.mods_title"));
        this.parent = parent;
        this.background = background;
    }

    private record Entry(String modId, Component name, @Nullable Component version) {
    }

    @Override
    protected void init() {
        this.entries.clear();
        // distinct mod ids that registered a config, ordered by display name
        Set<String> modIds = new LinkedHashSet<>();
        for (ModConfigHolder h : ModConfigHolder.getTrackedSpecs()) modIds.add(h.getModId());
        for (String modId : modIds) {
            String name = safe(() -> PlatHelper.getModName(modId), modId);
            String version = safe(() -> PlatHelper.getModVersion(modId), null);
            this.entries.add(new Entry(modId, Component.literal(name),
                    version == null ? null : Component.literal("v" + version)));
        }
        this.entries.sort(Comparator.comparing(e -> e.name().getString(), String.CASE_INSENSITIVE_ORDER));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void computeLayout() {
        int availWidth = this.width - 2 * SIDE_MARGIN;
        this.cols = Math.max(1, (availWidth + CARD_GAP) / (CARD_W + CARD_GAP));
        int gridWidth = this.cols * (CARD_W + CARD_GAP) - CARD_GAP;
        this.startX = (this.width - gridWidth) / 2;
        // the content panel spans header→footer, matching the config list screens (their list occupies the same band)
        this.contentTop = HEADER;
        this.contentBottom = this.height - FOOTER;

        int rows = (this.entries.size() + this.cols - 1) / this.cols;
        int totalHeight = Math.max(0, rows * (CARD_H + CARD_GAP) - CARD_GAP) + 2 * GRID_PAD;
        this.maxScroll = Math.max(0, totalHeight - (this.contentBottom - this.contentTop));
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);
    }

    private int cardX(int i) {
        return this.startX + (i % this.cols) * (CARD_W + CARD_GAP);
    }

    private int cardY(int i) {
        return this.contentTop + GRID_PAD + (i / this.cols) * (CARD_H + CARD_GAP) - (int) this.scroll;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        computeLayout();

        // the tiling list background behind the cards, matching the config list screens' scroll panel
        renderListBackground(graphics);

        boolean inViewport = mouseY >= contentTop && mouseY < contentBottom;
        graphics.enableScissor(0, contentTop, this.width, contentBottom);
        for (int i = 0; i < entries.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (y + CARD_H < contentTop || y > contentBottom) continue; // cull off-screen rows
            boolean hover = inViewport && mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H;
            renderCard(graphics, entries.get(i), x, y, hover);
        }
        graphics.disableScissor();

        // top/bottom inner-shadow separators framing the panel (drawn over the card edges, like the vanilla list)
        renderListSeparators(graphics);
        renderScrollbar(graphics);

        // header bar drawn last so cards scroll under it cleanly
        graphics.fill(0, 0, this.width, HEADER, HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, ConfigScreenLayout.HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER - this.font.lineHeight) / 2, TITLE_COLOR);
    }

    /** The 32×32 tiling list background over the scroll panel (mirrors {@code AbstractSelectionList#renderListBackground}). */
    private void renderListBackground(GuiGraphics graphics) {
        ResourceLocation bg = this.minecraft.level == null ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND;
        RenderSystem.enableBlend();
        graphics.blit(bg, 0, contentTop, (float) this.width, (float) (contentBottom + (int) this.scroll),
                this.width, contentBottom - contentTop, 32, 32);
        RenderSystem.disableBlend();
    }

    /** The top and bottom inner-shadow strips (mirrors {@code AbstractSelectionList#renderListSeparators}). */
    private void renderListSeparators(GuiGraphics graphics) {
        boolean inWorld = this.minecraft.level != null;
        ResourceLocation header = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        ResourceLocation footer = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(header, 0, contentTop - 2, 0f, 0f, this.width, 2, 32, 2);
        graphics.blit(footer, 0, contentBottom, 0f, 0f, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    private void renderCard(GuiGraphics graphics, Entry entry, int x, int y, boolean hover) {
        graphics.fill(x, y, x + CARD_W, y + CARD_H, hover ? CARD_BG_HOVER : CARD_BG);
        graphics.renderOutline(x, y, CARD_W, CARD_H, hover ? CARD_OUTLINE_HOVER : CARD_OUTLINE);

        int iconX = x + (CARD_W - ICON_SIZE) / 2;
        int iconY = y + CARD_PAD;
        ModIcons.Icon icon = ModIcons.get(entry.modId());
        if (icon != null) {
            graphics.blit(icon.texture(), iconX, iconY, ICON_SIZE, ICON_SIZE, 0f, 0f,
                    icon.width(), icon.height(), icon.width(), icon.height());
        } else {
            renderFallbackIcon(graphics, entry, iconX, iconY);
        }

        int textCenter = x + CARD_W / 2;
        int nameY = iconY + ICON_SIZE + ICON_TEXT_GAP;
        // name is centered but marquees when it's too long for the card
        GuiHelper.renderScrollingTextCentered(graphics, this.font, entry.name(), x + 4, x + CARD_W - 4, nameY, LINE, LABEL_COLOR);
        if (entry.version() != null) {
            drawClippedCentered(graphics, entry.version(), textCenter, nameY + LINE + NAME_VER_GAP, x + 4, x + CARD_W - 4, VERSION_COLOR);
        }
    }

    /** Centered text, scissor-clipped to [minX, maxX] so long names don't spill past the card. */
    private void drawClippedCentered(GuiGraphics graphics, Component text, int centerX, int y, int minX, int maxX, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + this.font.lineHeight + 1);
        graphics.drawCenteredString(this.font, text, centerX, y, color);
        graphics.disableScissor();
    }

    /** No declared icon: a dark tile with the mod's capital initial, falling back to the gear sprite for blanks. */
    private void renderFallbackIcon(GuiGraphics graphics, Entry entry, int iconX, int iconY) {
        graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0xFF303038);
        graphics.renderOutline(iconX, iconY, ICON_SIZE, ICON_SIZE, 0xFF000000);
        String name = entry.name().getString().trim();
        if (name.isEmpty()) {
            int g = ICON_SIZE - 10;
            graphics.blitSprite(GEAR_ICON, iconX + (ICON_SIZE - g) / 2, iconY + (ICON_SIZE - g) / 2, g, g);
            return;
        }
        String initial = name.substring(0, 1).toUpperCase();
        int tx = iconX + (ICON_SIZE - this.font.width(initial)) / 2;
        int ty = iconY + (ICON_SIZE - this.font.lineHeight) / 2;
        graphics.drawString(this.font, initial, tx, ty, CATEGORY_COLOR, false);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (maxScroll <= 0) return;
        int trackX = this.width - 6;
        int trackTop = contentTop, trackH = contentBottom - contentTop;
        int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
        int thumbY = trackTop + (int) ((trackH - thumbH) * (scroll / maxScroll));
        graphics.fill(trackX, trackTop, trackX + 3, trackTop + trackH, 0x40000000);
        graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFB0B0B0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            this.scroll = Mth.clamp(this.scroll - scrollY * (CARD_H / 2.0), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= contentTop && mouseY < contentBottom) {
            for (int i = 0; i < entries.size(); i++) {
                int x = cardX(i), y = cardY(i);
                if (mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H) {
                    Screen s = MoonlightConfigSelectScreen.create(entries.get(i).modId(), this, background);
                    if (s != null) {
                        this.minecraft.setScreen(s);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static String safe(ThrowingSupplier<String> supplier, String fallback) {
        try {
            String v = supplier.get();
            return v == null ? fallback : v;
        } catch (Exception e) {
            return fallback;
        }
    }
}
