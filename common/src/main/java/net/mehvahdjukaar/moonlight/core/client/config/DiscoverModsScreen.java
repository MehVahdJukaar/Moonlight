package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.OurModsList;
import net.mehvahdjukaar.moonlight.core.client.RemoteIconCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.FOOTER;
import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.HEADER;

public class DiscoverModsScreen extends Screen {

    private static final ResourceLocation INSTALLED_ICON = Moonlight.res("yes");
    private static final ResourceLocation GEAR_ICON = Moonlight.res("config");

    private static final int SIDE_MARGIN = 24;
    private static final int MAX_CONTENT_W = 320;
    private static final int ROW_H = 48;
    private static final int ROW_GAP = 4;
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
    private final List<Row> rows = new ArrayList<>();

    private LoadingDotsWidget loadingWidget;
    private boolean built;

    private double scroll;
    private int maxScroll;
    // recomputed each layout pass, shared by render + click
    private int contentTop, contentBottom, rowX, contentW;

    public DiscoverModsScreen(Screen parent) {
        super(Component.translatable("gui.moonlight.config.discover_title"));
        this.parent = parent;
    }

    private record Row(OurModsList.Entry data, boolean installed, List<FormattedCharSequence> descLines) {
    }

    @Override
    protected void init() {
        OurModsList.fetchIfNeeded();
        this.built = false;
        this.rows.clear();

        this.loadingWidget = new LoadingDotsWidget(this.font, Component.translatable("gui.moonlight.config.discover_loading"));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void buildRows() {
        this.rows.clear();
        this.contentW = Math.min(this.width - 2 * SIDE_MARGIN, MAX_CONTENT_W);
        int textWidth = this.contentW - (ROW_INNER_PAD + ICON_SIZE + ROW_INNER_PAD) - ROW_INNER_PAD;
        for (OurModsList.Entry e : OurModsList.getMods()) {
            boolean installed = PlatHelper.isModLoaded(e.modId());
            List<FormattedCharSequence> desc = e.description().isBlank()
                    ? List.of()
                    : this.font.split(Component.literal(e.description()), textWidth);
            if (desc.size() > MAX_DESC_LINES) desc = desc.subList(0, MAX_DESC_LINES);
            this.rows.add(new Row(e, installed, desc));
        }
        this.built = true;
    }

    private void computeLayout() {
        this.contentTop = HEADER;
        this.contentBottom = this.height - FOOTER;
        this.rowX = (this.width - this.contentW) / 2;
        int totalHeight = this.rows.size() * (ROW_H + ROW_GAP) - ROW_GAP + 2 * GRID_PAD;
        this.maxScroll = Math.max(0, totalHeight - (this.contentBottom - this.contentTop));
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);
    }

    private int rowY(int i) {
        return this.contentTop + GRID_PAD + i * (ROW_H + ROW_GAP) - (int) this.scroll;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.contentTop = HEADER;
        this.contentBottom = this.height - FOOTER;

        GuiHelper.renderListBackground(graphics, contentTop, contentBottom, this.width, this.scroll);

        OurModsList.State state = OurModsList.getState();
        if (state == OurModsList.State.LOADED) {
            if (!built) buildRows();
            renderRows(graphics, mouseX, mouseY);
        } else if (state == OurModsList.State.FAILED) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.moonlight.config.discover_offline"),
                    this.width / 2, (contentTop + contentBottom) / 2 - this.font.lineHeight / 2, ConfigGuiColors.DESCRIPTION);
        } else {
            // still fetching: center the vanilla loading-dots animation in the panel
            this.loadingWidget.setPosition(0, contentTop);
            this.loadingWidget.setSize(this.width, contentBottom - contentTop);
            this.loadingWidget.render(graphics, mouseX, mouseY, partialTick);
        }

        GuiHelper.renderFooterSeparator(graphics, contentBottom, this.width);
    }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY) {
        computeLayout();
        boolean inViewport = mouseY >= contentTop && mouseY < contentBottom;
        graphics.enableScissor(0, contentTop, this.width, contentBottom);
        for (int i = 0; i < rows.size(); i++) {
            int y = rowY(i);
            if (y + ROW_H < contentTop || y > contentBottom) continue; // cull off-screen rows
            boolean hover = inViewport && mouseX >= rowX && mouseX < rowX + contentW && mouseY >= y && mouseY < y + ROW_H;
            renderRow(graphics, rows.get(i), y, hover);
        }
        graphics.disableScissor();
        GuiHelper.renderScrollbar(graphics, contentTop, contentBottom, this.width, this.scroll, this.maxScroll);
    }

    private void renderRow(GuiGraphics graphics, Row row, int y, boolean hover) {
        graphics.fill(rowX, y, rowX + contentW, y + ROW_H, hover ? ConfigGuiColors.TILE_BG_HOVER : ConfigGuiColors.TILE_BG);
        graphics.renderOutline(rowX, y, contentW, ROW_H, hover ? ConfigGuiColors.TILE_OUTLINE_HOVER : ConfigGuiColors.TILE_OUTLINE);

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
            graphics.drawString(this.font, line, textX, descY, descColor);
            descY += LINE;
        }

        if (installed) {
            graphics.blitSprite(INSTALLED_ICON, textRight - 10, y + 7, 10, 10);
        }
    }

    private void renderIcon(GuiGraphics graphics, Row row, int iconX, int iconY, boolean installed) {
        // installed mods pull the icon straight from their jar; the rest fetch it from the catalog url
        ModIcons.Icon icon = ModIcons.get(row.data().modId());
        if (icon == null && row.data().iconUrl() != null) {
            icon = RemoteIconCache.get(row.data().modId(), row.data().iconUrl());
        }
        if (icon != null) {
            if (!installed) {
                RenderSystem.enableBlend();
                graphics.setColor(1f, 1f, 1f, 0.35f);
            }
            graphics.blit(icon.texture(), iconX, iconY, ICON_SIZE, ICON_SIZE, 0f, 0f,
                    icon.width(), icon.height(), icon.width(), icon.height());
            if (!installed) {
                graphics.setColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            }
        } else {
            renderFallbackIcon(graphics, row, iconX, iconY, installed);
        }
    }

    /**
     * No icon yet (missing, still downloading, or failed): a dark tile with the mod's initial, dimmed if not installed.
     */
    private void renderFallbackIcon(GuiGraphics graphics, Row row, int iconX, int iconY, boolean installed) {
        GuiHelper.renderInitialTile(graphics, this.font, row.data().name(), iconX, iconY, ICON_SIZE,
                installed ? ConfigGuiColors.TILE_ICON_BG : 0xFF25252B, installed ? ConfigGuiColors.CATEGORY : DESC_MISSING, GEAR_ICON);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && built && mouseY >= contentTop && mouseY < contentBottom) {
            for (int i = 0; i < rows.size(); i++) {
                int y = rowY(i);
                if (mouseX >= rowX && mouseX < rowX + contentW && mouseY >= y && mouseY < y + ROW_H) {
                    if (openModPage(rows.get(i).data())) return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean openModPage(OurModsList.Entry entry) {
        String url = entry.modrinthUrl() != null ? entry.modrinthUrl() : entry.curseforgeUrl();
        if (url == null) return false;
        GuiHelper.playClickSound();
        this.handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
