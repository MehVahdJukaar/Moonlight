package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.*;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.ItemCarouselWidget;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.minecraft.ChatFormatting;
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

    private static final int CAROUSEL_STRIP_WIDTH = 20;
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
        if (holders.size() == 1 && ConfigAvailability.of(holders.getFirst()).isEditable()
                && ConfigScreenExtensions.overlaysFor(modId).isEmpty()
                && ConfigScreenExtensions.showcaseFor(modId) == null
                && !ConfigScreenExtensions.hasFooterExtras(modId)) {
            return holders.getFirst().makeScreen(parent, background);
        }
        return new MoonlightConfigSelectScreen(modId, holders, parent, background);
    }

    @Override
    protected void init() {
        this.leftPaneWidth = Mth.clamp(this.width / 3, 104, 170);

        int blockWidth = this.leftPaneWidth - 2 * PAD;
        ConfigScreenExtensions.Showcase showcase = ConfigScreenExtensions.showcaseFor(this.modId);
        this.customShowcase = showcase != null;
        boolean showcaseTakesCarousel = showcase != null && showcase.replacesCarousel();
        if (showcase != null) {
            AbstractWidget widget = showcase.create(this.modId, PAD, this.iconTop(), blockWidth,
                    this.iconBottom() - this.iconTop() + (showcaseTakesCarousel ? CAROUSEL_STRIP_WIDTH + 4 : 0));
            this.addRenderableWidget(widget);
            this.leftPaneBottom = widget.getY() + widget.getHeight();
        }

        if (!showcaseTakesCarousel) {
            ItemCarouselWidget carousel = ClientConfigs.CONFIG_ITEM_CAROUSEL.get() ?
                    ItemCarouselWidget.forMod(this.modId, PAD, this.iconBottom() + 4, blockWidth, CAROUSEL_STRIP_WIDTH) : null;
            this.leftPaneBottom = this.iconBottom() + (carousel == null ? 0 : CAROUSEL_STRIP_WIDTH + 4);
            if (carousel != null) {
                this.addRenderableWidget(carousel.withOutline(ConfigGuiColors.TILE_OUTLINE));
            }
        }

        int paneWidth = this.width - this.leftPaneWidth;
        this.list = new ConfigRowList(this.minecraft, paneWidth, this.contentBottom() - HEADER, HEADER, SELECT_ITEM_HEIGHT);
        this.list.setX(this.leftPaneWidth);
        this.list.setRowWidth(Math.min(ROW_WIDTH, paneWidth - 28));
        this.list.setDrawFooterSeparator(false);
        this.list.setTopPadding((this.contentBottom() - HEADER - this.holders.size() * SELECT_ITEM_HEIGHT) / 2 - 4);

        List<ConfigRow> rows = new ArrayList<>();
        for (ModConfigHolder h : holders) {
            Component label = Component.literal(TextHelper.getReadableName(h.getId().getPath())).withStyle(ChatFormatting.BOLD);
            Component subtitle = Component.literal(h.getFileName());
            ConfigAvailability availability = ConfigAvailability.of(h);
            rows.add(new ConfigHolderRow(label, subtitle, configFileIcon(h.getConfigType()), availability.reason(),
                    () -> this.minecraft.setScreen(h.makeScreen(this, background))));
        }
        this.list.setRows(rows);
        this.addRenderableWidget(this.list);

        MediaButton.addAuthorMediaButtons(this, this::addRenderableWidget,
                this.width / 2, this.height - 28, 22, modId, this::onClose);
        this.addRenderableWidget(new GearButton(8, this.height - 28, 20,
                b -> this.minecraft.setScreen(new ModsTilesScreen(this, background))));
    }

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
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.version, this.width, HEADER);
        renderLeftPane(graphics);
    }

    private void renderLeftPane(GuiGraphics graphics) {
        int bottom = this.contentBottom();
        GuiHelper.renderListBackground(graphics, HEADER, bottom, this.leftPaneWidth, 0);

        int textWidth = this.leftPaneWidth - 2 * PAD;
        int iconHeight = Math.min(ICON_MAX, textWidth);

        if (!this.customShowcase) {
            Icon icon = ModIconCache.get(this.modId);
            if (icon != null) {
                GuiHelper.renderModIcon(graphics, icon, PAD, this.iconTop(), textWidth, iconHeight);
            } else {
                GuiHelper.renderInitialTile(graphics, this.font, this.title.getString(),
                        PAD + (textWidth - iconHeight) / 2, this.iconTop(), iconHeight,
                        ConfigGuiColors.TILE_LETTER_ICON_BG, ConfigGuiColors.initialLetterColor(this.modId), MoonlightIcons.CONFIG);
            }
        }
        int y = this.leftPaneBottom + 8;

        if (this.authors.isEmpty()) return;
        GuiHelper.renderSeparator(graphics, PAD, y, textWidth);
        y += 8;

        int line = this.font.lineHeight;
        graphics.drawString(this.font, Component.translatable("gui.moonlight.config.authors"), PAD, y,
                ConfigGuiColors.DESCRIPTION);
        y += line + 1;
        for (String author : this.authors) {
            for (FormattedCharSequence row : this.font.split(Component.literal(author), textWidth)) {
                if (y + line > bottom - 2) return;
                graphics.drawString(this.font, row, PAD, y, ConfigGuiColors.TEXT);
                y += line;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int bottom = this.contentBottom();
        GuiHelper.renderVerticalSeparator(graphics, this.leftPaneWidth, HEADER, bottom);
        GuiHelper.renderFooterSeparator(graphics, 0, bottom, this.width);

        ConfigScreenExtensions.Panel panel = overlayPanel();
        for (ConfigScreenExtensions.Overlay overlay : ConfigScreenExtensions.overlaysFor(modId)) {
            overlay.render(graphics, panel, mouseX, mouseY, partialTick);
        }
        ConfigRow hovered = this.list.getHovered(mouseX, mouseY);

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
