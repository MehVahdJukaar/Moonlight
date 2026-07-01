package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
public class MoonlightConfigScreen extends Screen implements ConfigScreenView, PopupHost {

    private final ConfigEditSession session;
    private final ConfigCategory category;
    @Nullable
    private final MoonlightConfigScreen parentConfig; // null = root level

    private ConfigOptionList list;
    private Button saveButton;
    private ConfigHeader header;
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

        // top bar: title + breadcrumb trail (walk up the parent chain) + search box
        List<ConfigHeader.Crumb> crumbs = new ArrayList<>();
        for (MoonlightConfigScreen s = this; s != null; s = s.parentConfig) {
            Component label = s.isRoot() ? Component.literal("⌂") : s.category.title(); // ⌂ home
            crumbs.add(0, new ConfigHeader.Crumb(label, s, s == this));
        }
        this.header = new ConfigHeader(this.font, this.width, session.holder().getReadableName(), crumbs,
                this.searchQuery, query -> {
            this.searchQuery = query;
            populate();
        });
        this.addWidget(this.header.searchBox());

        populate();
        this.addRenderableWidget(this.list);

        // Save (with live unsaved counter) sits next to Back on every page; the session is shared across the
        // whole navigation stack so Save here persists edits made in any sub category too.
        int y = this.height - 28;
        this.saveButton = new IconButton(this.width / 2 - 104, y, 100, 20, Component.empty(), SAVE_ICON, 12, 12, b -> doSave());
        this.addRenderableWidget(this.saveButton);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(this.width / 2 + 4, y, 100, 20).build());
        // bottom-left: icon-only jump to the mods hub grid
        IconButton modsButton = new IconButton(8, y, 20, 20, Component.empty(), CONFIG_ICON, 16, 16,
                b -> this.minecraft.setScreen(new ModsScreen(this, session.background()))).borderless();
        modsButton.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.mods_button")));
        this.addRenderableWidget(modsButton);
        refreshSave();
    }

    /**
     * (Re)builds the visible row list. Value rows whose description is expanded get read-only
     * {@link DescriptionRow}s inserted beneath them; expanded state lives in the session so it survives this.
     */
    private void populate() {
        List<ConfigRow> rows = new ArrayList<>();
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

    private void addOption(List<ConfigRow> rows, ConfigOption<?> v) {
        rows.add(new OptionRow(this, v));
        Component desc = v.description();
        if (desc != null && session.isExpanded(v)) {
            List<FormattedCharSequence> lines = this.font.split(desc, ROW_WIDTH - ARROW_WIDTH - 4);
            for (int i = 0; i < lines.size(); i += DESC_LINES_PER_ROW) {
                rows.add(new DescriptionRow(this.font,
                        lines.subList(i, Math.min(i + DESC_LINES_PER_ROW, lines.size()))));
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
        this.saveButton.setMessage(unsaved > 0
                ? Component.translatable("gui.moonlight.config.save_count", unsaved)
                : Component.translatable("gui.moonlight.config.save"));
        this.saveButton.active = unsaved > 0;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(isRoot() ? session.returnScreen() : parentConfig);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // an open dropdown popup floats above everything, so it gets first refusal on clicks
        if (overlay.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        Screen target = header.breadcrumbTarget(mouseX, mouseY);
        if (target != null && target != this) {
            this.minecraft.setScreen(target);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.header.render(graphics, this.width, mouseX, mouseY, this.font);

        // no row tooltips while a dropdown is open (its popup covers the rows)
        if (!overlay.isOpen()) {
            Component tooltip = null;
            ConfigRow hovered = this.list.getHovered(mouseX, mouseY);
            if (hovered != null) {
                tooltip = hovered.getTooltip(mouseX, mouseY);
            }
            // gutter decorations (e.g. reload-hint icons) sit outside the row hover band, so scan all rows for them
            if (tooltip == null) {
                for (ConfigRow row : this.list.children()) {
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
