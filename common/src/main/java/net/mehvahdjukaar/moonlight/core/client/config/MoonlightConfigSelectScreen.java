package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.ItemCarouselWidget;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

@ApiStatus.Internal
public class MoonlightConfigSelectScreen extends Screen {

    private static final int STRIP = 20;       // carousel strip under the mod icon
    private static final int PAD = 8;
    private static final int ICON_MAX = 64;

    private final String modId;
    private final Screen parent;
    @Nullable
    private final ResourceLocation background;
    private final List<ModConfigHolder> holders;
    @Nullable
    private final Component version;
    private final List<String> authors;

    private ConfigRowList list;
    private int leftPaneWidth;
    private int leftPaneBottom;
    private boolean customShowcase;

    private MoonlightConfigSelectScreen(String modId, List<ModConfigHolder> holders, Screen parent, @Nullable ResourceLocation background) {
        super(Component.literal(PlatHelper.getModName(modId)));
        this.modId = modId;
        this.parent = parent;
        this.background = background;
        this.holders = holders;
        String v = PlatHelper.getModVersion(modId);
        this.version = v == null ? null : Component.literal("v" + v);
        this.authors = PlatHelper.getModAuthors(modId);
    }

    private static List<ModConfigHolder> configsOf(String modId) {
        return ModConfigHolder.getTrackedHolders().stream()
                .filter(h -> h.getModId().equals(modId))
                .sorted(Comparator.comparingInt(h -> h.getConfigType().ordinal()))
                .toList();
    }

    @Nullable
    public static Screen create(String modId, Screen parent, @Nullable ResourceLocation background) {
        return create(modId, configsOf(modId), parent, background);
    }

    @Nullable
    public static Screen create(String modId, List<ModConfigHolder> holders, Screen parent, @Nullable ResourceLocation background) {
        if (holders.isEmpty()) return null;
        // a lone config doesn't need a list to pick from, unless an overlay, showcase or footer button would be
        // lost along with it
        if (holders.size() == 1 && ConfigScreenExtensions.overlaysFor(modId).isEmpty()
                && ConfigScreenExtensions.showcaseFor(modId) == null
                && !ConfigScreenExtensions.hasFooterExtras(modId)) {
            return holders.getFirst().makeScreen(parent, background);
        }
        return new MoonlightConfigSelectScreen(modId, holders, parent, background);
    }

    @Override
    protected void init() {
        // a third of the screen for the mod's identity, clamped so the config rows keep a usable width either way
        this.leftPaneWidth = Mth.clamp(this.width / 3, 104, 170);

        int blockWidth = this.leftPaneWidth - 2 * PAD;
        ConfigScreenExtensions.Showcase showcase = ConfigScreenExtensions.showcaseFor(this.modId);
        this.customShowcase = showcase != null;
        boolean showcaseTakesCarousel = showcase != null && showcase.replacesCarousel();
        if (showcase != null) {
            // the mod brought its own thing to put here, so the icon isn't drawn. The carousel goes too unless the
            // showcase asked to only fill the icon square
            AbstractWidget widget = showcase.create(this.modId, PAD, this.iconTop(), blockWidth,
                    this.iconBottom() - this.iconTop() + (showcaseTakesCarousel ? STRIP + 4 : 0));
            this.addRenderableWidget(widget);
            this.leftPaneBottom = widget.getY() + widget.getHeight();
        }
        if (!showcaseTakesCarousel) {
            ItemCarouselWidget carousel = ClientConfigs.CONFIG_ITEM_CAROUSEL.get() ?
                    ItemCarouselWidget.forMod(this.modId, PAD, this.iconBottom() + 4, blockWidth, STRIP) : null;
            this.leftPaneBottom = this.iconBottom() + (carousel == null ? 0 : STRIP + 4);
            if (carousel != null) {
                this.addRenderableWidget(carousel.withOutline(ConfigGuiColors.TILE_OUTLINE));
            }
        }

        int paneWidth = this.width - this.leftPaneWidth;
        this.list = new ConfigRowList(this.minecraft, paneWidth, this.contentBottom() - HEADER, HEADER, SELECT_ITEM_HEIGHT);
        this.list.setX(this.leftPaneWidth);
        this.list.setRowWidth(Math.min(ROW_WIDTH, paneWidth - 28)); // room for the scrollbar and a margin
        this.list.setDrawFooterSeparator(false); // this screen draws one across both panes instead
        // with only a handful of configs the rows read better centered in the pane than pinned under the header
        this.list.setTopPadding((this.contentBottom() - HEADER - this.holders.size() * SELECT_ITEM_HEIGHT) / 2 - 4);
        List<ConfigListRow> rows = new ArrayList<>();
        for (ModConfigHolder h : holders) {
            Component label = Component.literal(TextHelper.getReadableName(h.getId().getPath()));
            Component subtitle = Component.literal(h.getFileName());
            rows.add(new ConfigHolderRow(label, subtitle, configFileIcon(h.getConfigType()),
                    () -> this.minecraft.setScreen(h.makeScreen(this, background))));
        }
        this.list.setRows(rows);
        this.addRenderableWidget(this.list);

        // Back flanked by the author's media links, matching the Configured integration screen
        MediaButton.addAuthorMediaButtons(this, this::addRenderableWidget,
                this.width / 2, this.height - 28, 22, modId, this::onClose);
        this.addRenderableWidget(new GearButton(8, this.height - 28, 20,
                b -> this.minecraft.setScreen(new ModsTilesScreen(this, background))));
    }

    // bottom of the two panes
    private int contentBottom() {
        return this.height - FOOTER;
    }

    private int iconTop() {
        return HEADER + 10;
    }

    private int iconBottom() {
        return this.iconTop() + Math.min(ICON_MAX, this.leftPaneWidth - 2 * PAD);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        // header chrome in the background layer, behind the widgets (the list draws its own tiling background)
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.version, this.width, HEADER);
        renderLeftPane(graphics);
    }

    // the mod's identity pane: icon on top, authors under it, on the same flat background as the header
    private void renderLeftPane(GuiGraphics graphics) {
        int bottom = this.contentBottom();
        GuiHelper.renderMenuBand(graphics, 0, HEADER, this.leftPaneWidth, bottom - HEADER);

        int textWidth = this.leftPaneWidth - 2 * PAD;
        int iconHeight = Math.min(ICON_MAX, textWidth);

        if (!this.customShowcase) {
            ModIcons.Icon icon = ModIcons.get(this.modId);
            if (icon != null) {
                GuiHelper.renderModIcon(graphics, icon, PAD, this.iconTop(), textWidth, iconHeight);
            } else {
                GuiHelper.renderInitialTile(graphics, this.font, this.title.getString(),
                        PAD + (textWidth - iconHeight) / 2, this.iconTop(), iconHeight,
                        ConfigGuiColors.TILE_ICON_BG, ConfigGuiColors.initialLetter(this.modId), MoonlightIcons.CONFIG);
            }
        }
        // whatever fills the block above (carousel or mod showcase) is a widget, so it draws itself into this gap
        int y = this.leftPaneBottom + 8;

        if (this.authors.isEmpty()) return;
        GuiHelper.renderSeparator(graphics, PAD, y, textWidth);
        y += 8;

        // one name per line, wrapping long ones, filling whatever is left of the pane
        int line = this.font.lineHeight;
        graphics.drawString(this.font, Component.translatable("gui.moonlight.config.authors"), PAD, y,
                ConfigGuiColors.DESCRIPTION);
        y += line + 1;
        for (String author : this.authors) {
            for (FormattedCharSequence row : this.font.split(Component.literal(author), textWidth)) {
                if (y + line > bottom - 2) return;
                graphics.drawString(this.font, row, PAD, y, ConfigGuiColors.LABEL);
                y += line;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // dividers on top of the widget layer, else the list's own background paints over them
        int bottom = this.contentBottom();
        GuiHelper.renderVerticalSeparator(graphics, this.leftPaneWidth, HEADER, bottom);
        GuiHelper.renderFooterSeparator(graphics, bottom, this.width);

        ConfigScreenExtensions.Panel panel = overlayPanel();
        for (ConfigScreenExtensions.Overlay overlay : ConfigScreenExtensions.overlaysFor(modId)) {
            overlay.render(graphics, panel, mouseX, mouseY, partialTick);
        }
        // row tooltip on top of everything
        ConfigListRow hovered = this.list.getHovered(mouseX, mouseY);
        if (hovered != null) {
            Component tooltip = hovered.getTooltip(mouseX, mouseY);
            if (tooltip != null) {
                graphics.renderTooltip(this.font, this.font.split(tooltip, 220), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ConfigScreenExtensions.Panel panel = overlayPanel();
        for (ConfigScreenExtensions.Overlay overlay : ConfigScreenExtensions.overlaysFor(modId)) {
            if (overlay.mouseClicked(panel, mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ConfigScreenExtensions.Panel overlayPanel() {
        return new ConfigScreenExtensions.Panel(this, 0, HEADER, this.width, this.contentBottom());
    }
}
