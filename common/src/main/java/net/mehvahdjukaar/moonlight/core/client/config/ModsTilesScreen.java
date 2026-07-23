package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.misc.ThrowingSupplier;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
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

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

public class ModsTilesScreen extends Screen {

    private static final ResourceLocation GEAR_ICON = Moonlight.res("config");
    // mods that don't use Moonlight's config system but that we still surface here, opened via the loader's own
    // config screen (NeoForge screen extension, or Mod Menu on Fabric). Only shown when such a screen exists.
    private static final List<String> EXTRA_MODS = List.of("polytone", "nautilus_studio");

    private static final int GRID_PAD = 8; // inset for the first/last card row inside the scroll panel

    private static final int CARD_W = 88;
    private static final int CARD_PAD = 9;        // equal padding above the icon and below the last text line
    private static final int ICON_TEXT_GAP = 6;   // icon → name
    private static final int NAME_VER_GAP = 2;    // name → version
    private static final int ICON_SIZE = 32;      // icon slot height; square icons render at this, wider ones expand
    private static final int ICON_SIDE_PAD = 8;   // min horizontal padding kept between a wide icon and the tile edge
    private static final int LINE = 9;            // vanilla font line height
    // top pad + icon + gap + name + gap + version + bottom pad — kept balanced (CARD_PAD on both ends)
    private static final int CARD_H = CARD_PAD + ICON_SIZE + ICON_TEXT_GAP + LINE + NAME_VER_GAP + LINE + CARD_PAD;
    private static final int CARD_GAP = 6;
    private static final int SIDE_MARGIN = 24;

    private static final int VERSION_COLOR = ConfigGuiColors.DESCRIPTION;

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
        // extra mods (and, if enabled, every installed mod) that expose a loader/Mod Menu config screen
        for (String modId : EXTRA_MODS) {
            if (ClientHelper.hasModConfigScreen(modId)) modIds.add(modId);
        }
        // converting foreign configs implies showing every mod's tile, so you can actually reach them. In that mode a
        // mod also qualifies if it only has a raw (loader) config we can convert, even without its own screen
        boolean convert = ClientConfigs.CONVERT_FOREIGN_CONFIGS.get();
        if (ClientConfigs.SHOW_ALL_MOD_CONFIGS.get() || convert) {
            for (String modId : PlatHelper.getInstalledMods()) {
                if (ClientHelper.hasModConfigScreen(modId) || (convert && ClientHelper.hasNativeForeignConfig(modId))) {
                    modIds.add(modId);
                }
            }
        }
        for (String modId : modIds) {
            String name = safe(() -> PlatHelper.getModName(modId), modId);
            String version = safe(() -> PlatHelper.getModVersion(modId), null);
            this.entries.add(new Entry(modId, Component.literal(name),
                    version == null ? null : Component.literal("v" + version)));
        }
        this.entries.sort(Comparator.comparing(e -> e.name().getString(), String.CASE_INSENSITIVE_ORDER));

        this.addRenderableWidget(Button.builder(Component.translatable("gui.moonlight.config.discover_mods"),
                        b -> this.minecraft.setScreen(new DiscoverModsScreen(this)))
                .bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        // header chrome in the background layer; the card grid is scissored below HEADER, so cards slide under the bar
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        computeLayout();

        // the tiling list background behind the cards, matching the config list screens' scroll panel
        GuiHelper.renderListBackground(graphics, contentTop, contentBottom, this.width, this.scroll);

        boolean inViewport = mouseY >= contentTop && mouseY < contentBottom;
        graphics.enableScissor(0, contentTop, this.width, contentBottom);
        for (int i = 0; i < entries.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (y + CARD_H < contentTop || y > contentBottom) continue; // cull off-screen rows
            boolean hover = inViewport && mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H;
            renderCard(graphics, entries.get(i), x, y, hover);
        }
        graphics.disableScissor();

        // bottom inner-shadow separator framing the panel (the top edge is covered by the header bar)
        GuiHelper.renderFooterSeparator(graphics, contentBottom, this.width);
        GuiHelper.renderScrollbar(graphics, contentTop, contentBottom, this.width, this.scroll, this.maxScroll);
    }

    private void renderCard(GuiGraphics graphics, Entry entry, int x, int y, boolean hover) {
        graphics.fill(x, y, x + CARD_W, y + CARD_H, hover ? ConfigGuiColors.TILE_BG_HOVER : ConfigGuiColors.TILE_BG);
        graphics.renderOutline(x, y, CARD_W, CARD_H, hover ? ConfigGuiColors.TILE_OUTLINE_HOVER : ConfigGuiColors.TILE_OUTLINE);

        int iconX = x + (CARD_W - ICON_SIZE) / 2;
        int iconY = y + CARD_PAD;
        ModIcons.Icon icon = ModIcons.get(entry.modId());
        if (icon != null) {
            // draw at the real aspect ratio: height fills the icon slot, width grows for wider logos. If that would
            // spill past the tile's side padding, scale the whole thing down instead so it always fits.
            int maxW = CARD_W - 2 * ICON_SIDE_PAD;
            int h = ICON_SIZE;
            int w = Math.round(ICON_SIZE * (icon.width() / (float) icon.height()));
            if (w > maxW) {
                w = maxW;
                h = Math.round(maxW * (icon.height() / (float) icon.width()));
            }
            int dx = x + (CARD_W - w) / 2;                 // centered horizontally in the tile
            int dy = iconY + (ICON_SIZE - h) / 2;          // centered within the square icon slot
            graphics.blit(icon.texture(), dx, dy, w, h, 0f, 0f,
                    icon.width(), icon.height(), icon.width(), icon.height());
        } else {
            renderFallbackIcon(graphics, entry, iconX, iconY);
        }

        int textCenter = x + CARD_W / 2;
        int nameY = iconY + ICON_SIZE + ICON_TEXT_GAP;
        // name is centered but marquees when it's too long for the card
        GuiHelper.renderScrollingTextCentered(graphics, this.font, entry.name(), x + 4, x + CARD_W - 4, nameY, LINE, ConfigGuiColors.LABEL);
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
        GuiHelper.renderInitialTile(graphics, this.font, entry.name().getString(),
                iconX, iconY, ICON_SIZE, ConfigGuiColors.TILE_ICON_BG, ConfigGuiColors.CATEGORY, GEAR_ICON);
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
                    String modId = entries.get(i).modId();
                    // Moonlight-tracked mods open our own screen; else, when enabled, try to convert the mod's own
                    // config into a native screen; failing that, defer to the loader/Mod Menu screen it registered
                    Screen s = MoonlightConfigSelectScreen.create(modId, this, background);
                    if (s == null && ClientConfigs.CONVERT_FOREIGN_CONFIGS.get()) {
                        s = ClientHelper.getNativeForeignConfigScreen(modId, this, background);
                    }
                    if (s == null) s = ClientHelper.getModConfigScreen(modId, this);
                    if (s != null) {
                        GuiHelper.playClickSound();
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

    private static String safe(ThrowingSupplier<String> supplier, String fallback) {
        try {
            String v = supplier.get();
            return v == null ? fallback : v;
        } catch (Exception e) {
            return fallback;
        }
    }
}
