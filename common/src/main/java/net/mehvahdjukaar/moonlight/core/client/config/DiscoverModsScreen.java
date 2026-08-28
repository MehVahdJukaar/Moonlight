package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ModCatalogAPI;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.client.RemoteIconCache;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.FOOTER;
import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.HEADER;


public class DiscoverModsScreen extends Screen {


    private static final int SIDE_MARGIN = 24;
    private static final int MAX_CONTENT_W = 320;
    private static final int ROW_H = 48;
    private static final int ROW_GAP = 4;
    private static final int SECTION_H = 18;
    private static final int GRID_PAD = 8;
    private static final int ICON_SIZE = 32;
    private static final int ROW_INNER_PAD = 8;
    private static final int LINE = 11;
    private static final int MAX_DESC_LINES = 2;

    private static final int NAME_INSTALLED = ConfigGuiColors.LABEL;
    private static final int NAME_MISSING = ConfigGuiColors.TEXT_SECONDARY;
    private static final int DESC_INSTALLED = ConfigGuiColors.DESCRIPTION;
    private static final int DESC_MISSING = 0xFF6A6A78;

    private final Screen parent;
    private final List<ModCatalogAPI.Catalog> catalogs;
    private final List<Item> items = new ArrayList<>();

    private LoadingDotsWidget loadingWidget;
    private int builtFrom = -1; // how many mods had landed when the item list was built

    private double scroll;
    private int maxScroll;
    // recomputed each layout pass, shared by render + click
    private int contentTop, contentBottom, rowX, contentW;

    public DiscoverModsScreen(Screen parent) {
        this(parent, ModCatalogAPI.getCatalogs());
    }

    private DiscoverModsScreen(Screen parent, List<ModCatalogAPI.Catalog> catalogs) {
        super(titleFor(catalogs));
        this.parent = parent;
        this.catalogs = catalogs;
    }

    // one author gets their name in the title, several share the screen so it goes generic and each gets a section
    private static Component titleFor(List<ModCatalogAPI.Catalog> catalogs) {
        if (catalogs.size() == 1) {
            return Component.translatable("gui.moonlight.config.discover_title_by", catalogs.getFirst().author());
        }
        return Component.translatable("gui.moonlight.config.discover_title");
    }

    private sealed interface Item {
        int height();
    }

    private record Section(Component title) implements Item {
        @Override
        public int height() {
            return SECTION_H;
        }
    }

    private record Row(ModCatalogAPI.Entry data, boolean installed, List<FormattedCharSequence> descLines) implements Item {
        @Override
        public int height() {
            return ROW_H;
        }
    }

    @Override
    protected void init() {
        for (ModCatalogAPI.Catalog c : catalogs) c.onScreenOpened();
        this.builtFrom = -1;
        this.items.clear();

        this.loadingWidget = new LoadingDotsWidget(this.font, Component.translatable("gui.moonlight.config.discover_loading"));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private int modCount() {
        int count = 0;
        for (ModCatalogAPI.Catalog c : catalogs) count += c.mods().size();
        return count;
    }

    private boolean anyLoading() {
        for (ModCatalogAPI.Catalog c : catalogs) {
            if (c.isLoading()) return true;
        }
        return false;
    }

    private void buildItems() {
        this.items.clear();
        this.contentW = Math.min(this.width - 2 * SIDE_MARGIN, MAX_CONTENT_W);
        int textWidth = this.contentW - (ROW_INNER_PAD + ICON_SIZE + ROW_INNER_PAD) - ROW_INNER_PAD;
        boolean sections = this.catalogs.size() > 1;
        for (ModCatalogAPI.Catalog catalog : this.catalogs) {
            if (catalog.mods().isEmpty()) continue;
            if (sections) {
                this.items.add(new Section(Component.translatable("gui.moonlight.config.discover_by", catalog.author())));
            }
            for (ModCatalogAPI.Entry e : catalog.mods()) {
                boolean installed = PlatHelper.isModLoaded(e.modId());
                List<FormattedCharSequence> desc = e.description().isBlank()
                        ? List.of()
                        : this.font.split(Component.literal(e.description()), textWidth);
                if (desc.size() > MAX_DESC_LINES) desc = desc.subList(0, MAX_DESC_LINES);
                this.items.add(new Row(e, installed, desc));
            }
        }
        this.builtFrom = modCount();
    }

    private void computeLayout() {
        this.contentTop = HEADER;
        this.contentBottom = this.height - FOOTER;
        this.rowX = (this.width - this.contentW) / 2;
        int totalHeight = totalHeight() + 2 * GRID_PAD;
        this.maxScroll = Math.max(0, totalHeight - (this.contentBottom - this.contentTop));
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);
    }

    private int totalHeight() {
        int h = 0;
        for (Item item : this.items) h += item.height() + ROW_GAP;
        return Math.max(0, h - ROW_GAP);
    }

    private int itemY(int index) {
        int y = this.contentTop + GRID_PAD - (int) this.scroll;
        for (int i = 0; i < index; i++) y += this.items.get(i).height() + ROW_GAP;
        return y;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.contentTop = HEADER;
        this.contentBottom = this.height - FOOTER;

        GuiHelper.renderListBackground(graphics, contentTop, contentBottom, this.width, this.scroll);

        int mods = modCount();
        if (mods > 0) {
            // catalogs land one at a time, so rebuild whenever another one shows up
            if (mods != this.builtFrom) buildItems();
            renderItems(graphics, mouseX, mouseY);
        } else if (!anyLoading()) {
            graphics.centeredText(this.font, Component.translatable("gui.moonlight.config.discover_offline"),
                    this.width / 2, (contentTop + contentBottom) / 2 - this.font.lineHeight / 2, ConfigGuiColors.DESCRIPTION);
        } else {
            // still fetching: center the vanilla loading-dots animation in the panel
            this.loadingWidget.setPosition(0, contentTop);
            this.loadingWidget.setSize(this.width, contentBottom - contentTop);
            this.loadingWidget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        GuiHelper.renderFooterSeparator(graphics, contentBottom, this.width);
    }

    private void renderItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        computeLayout();
        boolean inViewport = mouseY >= contentTop && mouseY < contentBottom;
        graphics.enableScissor(0, contentTop, this.width, contentBottom);
        int y = this.contentTop + GRID_PAD - (int) this.scroll;
        for (Item item : items) {
            int h = item.height();
            if (y + h >= contentTop && y <= contentBottom) { // cull off-screen items
                if (item instanceof Section section) {
                    renderSection(graphics, section, y);
                } else if (item instanceof Row row) {
                    boolean hover = inViewport && mouseX >= rowX && mouseX < rowX + contentW && mouseY >= y && mouseY < y + h;
                    renderRow(graphics, row, y, hover);
                }
            }
            y += h + ROW_GAP;
        }
        graphics.disableScissor();
        GuiHelper.renderScrollbar(graphics, contentTop, contentBottom, this.width, this.scroll, this.maxScroll);
    }

    private void renderSection(GuiGraphicsExtractor graphics, Section section, int y) {
        int textY = y + SECTION_H - this.font.lineHeight - 2;
        graphics.text(this.font, section.title(), rowX, textY, ConfigGuiColors.TEXT_SECONDARY);
        GuiHelper.renderSeparator(graphics, rowX, textY + this.font.lineHeight + 1, contentW);
    }

    private void renderRow(GuiGraphicsExtractor graphics, Row row, int y, boolean hover) {
        graphics.fill(rowX, y, rowX + contentW, y + ROW_H, hover ? ConfigGuiColors.TILE_BG_HOVER : ConfigGuiColors.TILE_BG);
        graphics.outline(rowX, y, contentW, ROW_H, hover ? ConfigGuiColors.TILE_OUTLINE_HOVER : ConfigGuiColors.TILE_OUTLINE);

        boolean installed = row.installed();
        int iconX = rowX + ROW_INNER_PAD;
        int iconY = y + (ROW_H - ICON_SIZE) / 2;
        renderIcon(graphics, row, iconX, iconY, installed);

        int textX = iconX + ICON_SIZE + ROW_INNER_PAD;
        int textRight = rowX + contentW - ROW_INNER_PAD;
        // installed mods get a small check on the far right; leave room for it on the name line
        int nameRight = installed ? textRight - 12 : textRight;

        int nameColor = installed ? NAME_INSTALLED : NAME_MISSING;
        GuiHelper.renderScrollingText(graphics, this.font, Component.literal(row.data().name()),
                textX, nameRight, y + 6, LINE, nameColor);

        int descColor = installed ? DESC_INSTALLED : DESC_MISSING;
        int descY = y + 6 + LINE;
        for (FormattedCharSequence line : row.descLines()) {
            graphics.text(this.font, line, textX, descY, descColor);
            descY += LINE;
        }

        if (installed) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MoonlightIcons.YES, textRight - 10, y + 7, 10, 10);
        }
    }

    private void renderIcon(GuiGraphicsExtractor graphics, Row row, int iconX, int iconY, boolean installed) {
        // installed mods pull the icon straight from their jar; the rest fetch it from the catalog url
        ModIcons.Icon icon = ModIcons.get(row.data().modId());
        if (icon == null && row.data().iconUrl() != null) {
            icon = RemoteIconCache.get(row.data().modId(), row.data().iconUrl());
        }
        if (icon != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), iconX, iconY, 0f, 0f, ICON_SIZE, ICON_SIZE,
                    icon.width(), icon.height(), icon.width(), icon.height(),
                    installed ? 0xFFFFFFFF : ARGB.white(0.35f));
        } else {
            renderFallbackIcon(graphics, row, iconX, iconY, installed);
        }
    }

    // no icon yet (missing, downloading or failed): a dark tile with the mod's initial, dimmed if not installed
    private void renderFallbackIcon(GuiGraphicsExtractor graphics, Row row, int iconX, int iconY, boolean installed) {
        GuiHelper.renderInitialTile(graphics, this.font, row.data().name(), iconX, iconY, ICON_SIZE,
                installed ? ConfigGuiColors.TILE_ICON_BG : 0xFF25252B,
                installed ? ConfigGuiColors.initialLetter(row.data().name()) : DESC_MISSING, MoonlightIcons.CONFIG);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            this.scroll = Mth.clamp(this.scroll - scrollY * (ROW_H / 2.0), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        if (button == 0 && mouseY >= contentTop && mouseY < contentBottom) {
            Row clicked = rowAt(mouseX, mouseY);
            if (clicked != null && openModPage(clicked.data())) return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Nullable
    private Row rowAt(double mouseX, double mouseY) {
        if (mouseX < rowX || mouseX >= rowX + contentW) return null;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (!(item instanceof Row row)) continue;
            int y = itemY(i);
            if (mouseY >= y && mouseY < y + row.height()) return row;
        }
        return null;
    }

    private boolean openModPage(ModCatalogAPI.Entry entry) {
        String url = entry.curseforgeUrl() != null ? entry.curseforgeUrl() : entry.modrinthUrl();
        if (url == null) return false;
        GuiHelper.playClickSound();
        ConfirmLinkScreen.confirmLinkNow(this, url);
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
