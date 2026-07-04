package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.minecraft.ChatFormatting;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * Native, loader independent Moonlight config screen. Renders a server safe {@link ConfigCategory} tree
 * (produced by the config holder) using vanilla list screens. It only handles layout and navigation: rows live
 * in their own classes, widgets come from the {@link ConfigControls} registry and edit state lives in a
 * {@link ConfigEditSession} shared across the navigation stack, so this class stays small and the system is open
 * for new control types without touching it.
 */
public class MoonlightConfigScreen extends Screen implements ConfigScreenAccess, PopupHost {

    // reused so re-opening/leaving repeatedly refreshes one toast instead of stacking duplicates
    private static final SystemToast.SystemToastId RELOAD_TOAST_ID = new SystemToast.SystemToastId();

    private final ConfigEditSession session;
    private final ConfigCategory category;
    @Nullable
    private final MoonlightConfigScreen parentConfig; // null = root level

    // top-bar geometry: title centered on the first line, breadcrumb + search on the second
    private static final int SIDE_MARGIN = 14;
    private static final int CRUMB_Y = 25;
    private static final int SEARCH_WIDTH = 110;
    private static final int SEARCH_HEIGHT = 14;
    private static final int SEARCH_ICON_SIZE = 10;

    private ConfigOptionList list;
    private Button saveButton;
    private EditBox searchBox;
    private String searchQuery = "";
    private final OverlayLayer overlay = new OverlayLayer(); // floats an open dropdown/popup above the list

    /**
     * Root entry point: starts a fresh editing session for the whole config.
     */
    public MoonlightConfigScreen(ModConfigHolder holder, ConfigCategory root, Screen returnScreen,
                                 @Nullable ResourceLocation background) {
        this(root, null, new ConfigEditSession(holder, returnScreen, background));
    }

    private MoonlightConfigScreen(ConfigCategory category, @Nullable MoonlightConfigScreen parentConfig, ConfigEditSession session) {
        super(parentConfig == null ? session.holder().getReadableName() : category.title());
        this.category = category;
        this.parentConfig = parentConfig;
        this.session = session;
    }

    private boolean isRoot() {
        return parentConfig == null;
    }

    // ===== ConfigScreenView =====

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public ConfigEditSession session() {
        return this.session;
    }

    @Override
    public void openCategory(ConfigCategory cat) {
        this.minecraft.setScreen(new MoonlightConfigScreen(cat, this, session));
    }

    @Override
    public void toggleExpanded(ConfigOption<?> value) {
        session.toggleExpanded(value);
        populate();
    }

    @Override
    public void onValueEdited() {
        refreshSave();
    }

    @Override
    public boolean isCategoryEnabled(ConfigCategory cat) {
        ConfigOption.BooleanValue gate = cat.gate();
        boolean own = gate == null || Boolean.TRUE.equals(session.current(gate));
        ConfigCategory parent = cat.parent();
        return own && (parent == null || isCategoryEnabled(parent));
    }

    // ===== screen =====

    @Override
    public OverlayLayer getOverlayLayer() {
        return this.overlay;
    }

    @Override
    protected void init() {
        this.overlay.clear(); // widgets are rebuilt here, so drop any stale open popup
        this.list = new ConfigOptionList(this.minecraft, this.width, this.height - HEADER - FOOTER, HEADER, ITEM_HEIGHT);

        // top bar: a search box on the right, then a breadcrumb trail (walk up the parent chain) filling the space
        // to its left. Both are registered for input here but drawn in render(), on top of the header bar.
        this.searchBox = new EditBox(this.font, this.width - SIDE_MARGIN - SEARCH_WIDTH, CRUMB_Y - 3,
                SEARCH_WIDTH, SEARCH_HEIGHT, Component.translatable("gui.moonlight.config.search"));
        this.searchBox.setHint(Component.translatable("gui.moonlight.config.search")
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setResponder(query -> {
            this.searchQuery = query;
            populate();
        });
        this.addRenderableWidget(this.searchBox);

        List<BreadcrumbWidget.Crumb> crumbs = new ArrayList<>();
        for (MoonlightConfigScreen s = this; s != null; s = s.parentConfig) {
            Component label = s.isRoot() ? Component.literal("⌂") : s.category.title(); // ⌂ home
            crumbs.addFirst(new BreadcrumbWidget.Crumb(label, s, s == this));
        }
        int trailRight = this.searchBox.getX() - SEARCH_ICON_SIZE - 6; // leave room for the magnifier glyph + a gap
        BreadcrumbWidget breadcrumb = new BreadcrumbWidget(SIDE_MARGIN, CRUMB_Y, trailRight - SIDE_MARGIN, this.font.lineHeight,
                this.font, crumbs, target -> {
            if (target != this) this.minecraft.setScreen(target);
        });
        this.addRenderableWidget(breadcrumb);

        populate();
        this.addRenderableWidget(this.list);

        // Bottom bar: [Reset all] Save | Back, all the same size. Save (with its live unsaved counter) and Back
        // are on every page; the session is shared across the navigation stack so Save persists sub-category edits
        // too. Reset all only shows at the root/home page, since it acts on the whole config.
        int y = this.height - 28;
        int bw = 100, gap = 4;
        if (isRoot()) {
            int total = 3 * bw + 2 * gap;
            int x0 = (this.width - total) / 2;
            this.addRenderableWidget(new IconButton(x0, y, bw, 20,
                    Component.translatable("gui.moonlight.config.reset_all"), RESET_ICON, 12, 12, b -> confirmResetAll()));
            this.saveButton = new IconButton(x0 + bw + gap, y, bw, 20, Component.empty(), SAVE_ICON, 12, 12, b -> doSave());
            this.addRenderableWidget(this.saveButton);
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(x0 + 2 * (bw + gap), y, bw, 20).build());
        } else {
            this.saveButton = new IconButton(this.width / 2 - 104, y, bw, 20, Component.empty(), SAVE_ICON, 12, 12, b -> doSave());
            this.addRenderableWidget(this.saveButton);
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(this.width / 2 + 4, y, bw, 20).build());
        }
        IconButton modsButton = new IconButton(8, y, 20, 20, Component.empty(), CONFIG_ICON, 16, 16,
                b -> this.minecraft.setScreen(new ModsTilesScreen(this, session.background()))).borderless();
        modsButton.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.mods_button")));
        this.addRenderableWidget(modsButton);
        refreshSave();
    }

    /** Asks for confirmation, then resets every value in the config to its default and saves immediately. */
    private void confirmResetAll() {
        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                resetAllToDefaults(this.category);
                session.apply();        // reset writes straight through, like the per-row reset + Save
                session.clearPending();
            }
            this.minecraft.setScreen(this); // re-inits, so rows re-read the (now saved) values
        }, Component.translatable("gui.moonlight.config.reset_all.title"),
                Component.translatable("gui.moonlight.config.reset_all.message")));
    }

    /** Recursively stages the default value of every editable option in {@code cat} and its sub-categories. */
    private void resetAllToDefaults(ConfigCategory cat) {
        for (ConfigNode e : cat.entries()) {
            if (e instanceof ConfigCategory sub) {
                resetAllToDefaults(sub);
            } else if (e instanceof ConfigOption<?> v && !(v instanceof ConfigOption.UnsupportedValue)) {
                session.put(v, v.defaultValue());
            }
        }
    }

    /**
     * (Re)builds the visible row list. Value rows whose description is expanded get read-only
     * {@link DescriptionRow}s inserted beneath them; expanded state lives in the session so it survives this.
     */
    private void populate() {
        List<ConfigListRow> rows = new ArrayList<>();
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            for (ConfigNode e : category.entries()) {
                if (e == category.gate()) continue; // shown as the category's inline toggle, not a duplicate row
                if (e instanceof ConfigCategory cat) {
                    rows.add(new CategoryRow(this, cat));
                } else if (e instanceof ConfigOption<?> v) {
                    addOption(rows, v);
                }
            }
        } else {
            // flat, recursive search across this category's whole subtree
            List<ConfigOption<?>> matches = new ArrayList<>();
            collectMatches(category, query, matches);
            for (ConfigOption<?> v : matches) {
                addOption(rows, v);
            }
        }
        this.list.setRows(rows);
    }

    private void addOption(List<ConfigListRow> rows, ConfigOption<?> v) {
        rows.add(new OptionRow(this, v));
        if (v.description() != null && session.isExpanded(v)) {
            List<FormattedCharSequence> lines = this.font.split(v.description(), ROW_WIDTH - ARROW_WIDTH - GAP);
            for (int i = 0; i < lines.size(); i += DESC_LINES_PER_ROW) {
                rows.add(new DescriptionRow(this.font, lines.subList(i, Math.min(i + DESC_LINES_PER_ROW, lines.size()))));
            }
        }
    }

    private static void collectMatches(ConfigCategory category, String query, List<ConfigOption<?>> out) {
        for (ConfigNode e : category.entries()) {
            if (e == category.gate()) continue; // gate is edited via its category's inline toggle
            if (e instanceof ConfigCategory cat) {
                collectMatches(cat, query, out);
            } else if (e instanceof ConfigOption<?> v) {
                if (v.title().getString().toLowerCase(Locale.ROOT).contains(query)) out.add(v);
            }
        }
    }

    private void doSave() {
        session.apply();
        session.clearPending();
        this.rebuildWidgets(); // re-read saved values, reset counter and rollback buttons
    }

    private void refreshSave() {
        if (this.saveButton == null) return;
        int unsaved = session.unsavedCount();
        // the "(N)" unsaved counter is tinted amber to draw the eye when there are pending edits
        Component count = Component.literal("(" + unsaved + ")")
                .withStyle(s -> s.withColor(TextColor.fromRgb(ConfigGuiColors.MODIFIED)));
        this.saveButton.setMessage(unsaved > 0
                ? Component.translatable("gui.moonlight.config.save_count", count)
                : Component.translatable("gui.moonlight.config.save"));
        this.saveButton.active = unsaved > 0;
    }

    @Override
    public void onClose() {
        // leaving the config entirely (root → return screen) with pending edits would silently drop them: confirm first.
        // going back to a parent category stays within the shared session, so nothing is lost and no prompt is needed.
        if (isRoot() && session.unsavedCount() > 0) {
            this.minecraft.setScreen(new ConfirmScreen(discard -> {
                        if (discard) leaveConfig();
                        else this.minecraft.setScreen(this);
                    },
                    Component.translatable("gui.moonlight.config.discard.title"),
                    Component.translatable("gui.moonlight.config.discard.message", session.unsavedCount()),
                    Component.translatable("gui.moonlight.config.discard.confirm"), CommonComponents.GUI_CANCEL));
            return;
        }
        if (isRoot()) leaveConfig();
        else this.minecraft.setScreen(parentConfig);
    }

    /** Returns to the screen the config was opened from, warning (via toast) if any saved change needs a reload. */
    private void leaveConfig() {
        ConfigReloadType reload = session.appliedReload();
        if (reload != ConfigReloadType.NONE) {
            Component message = Component.translatable(reload == ConfigReloadType.GAME_RESTART
                    ? "gui.moonlight.config.reload_needed.game" : "gui.moonlight.config.reload_needed.world");
            this.minecraft.getToasts().addToast(SystemToast.multiline(this.minecraft, RELOAD_TOAST_ID,
                    Component.translatable("gui.moonlight.config.reload_needed.title"), message));
        }
        this.minecraft.setScreen(session.returnScreen());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // an open dropdown popup floats above everything, so it gets first refusal on clicks
        if (overlay.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // an open dropdown is modal: it scrolls its own popup and swallows the wheel so the row list stays put
        if (overlay.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (overlay.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (overlay.charTyped(c, modifiers)) {
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        // header chrome sits in the background layer (drawn before the widgets by super.render): the bar, its
        // separator, the title and the search magnifier. The row list is scissored below HEADER, so rows slide
        // under the bar cleanly; the breadcrumb and search box are renderable widgets drawn on top of it.
        graphics.fill(0, 0, this.width, HEADER, ConfigGuiColors.HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 7, ConfigGuiColors.TITLE);
        graphics.blitSprite(SEARCH_ICON, this.searchBox.getX() - SEARCH_ICON_SIZE - 2,
                this.searchBox.getY() + (SEARCH_HEIGHT - SEARCH_ICON_SIZE) / 2, SEARCH_ICON_SIZE, SEARCH_ICON_SIZE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // no row tooltips while a dropdown is open (its popup covers the rows)
        if (!overlay.isOpen()) {
            Component tooltip = null;
            ConfigListRow hovered = this.list.getHovered(mouseX, mouseY);
            if (hovered != null) {
                tooltip = hovered.getTooltip(mouseX, mouseY);
            }
            // gutter decorations (e.g. reload-hint icons) sit outside the row hover band, so scan all rows for them
            if (tooltip == null) {
                for (ConfigListRow row : this.list.children()) {
                    tooltip = row.getGutterTooltip(mouseX, mouseY);
                    if (tooltip != null) break;
                }
            }
            if (tooltip != null) {
                graphics.renderTooltip(this.font, this.font.split(tooltip, 220), mouseX, mouseY);
            }
        } else {
            overlay.render(graphics, mouseX, mouseY);
        }
    }
}
