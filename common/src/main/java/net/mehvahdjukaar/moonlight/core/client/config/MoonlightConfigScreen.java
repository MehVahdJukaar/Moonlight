package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.BreadcrumbWidget;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.SearchBoxWidget;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

public class MoonlightConfigScreen extends ConfigPageScreen {

    //one id so leaving repeatedly doesn't stack toasts
    private static final SystemToast.SystemToastId RELOAD_TOAST_ID = new SystemToast.SystemToastId();

    private final ConfigEditSession session;
    private final ConfigCategory category;
    @Nullable
    private final MoonlightConfigScreen parentPage; // null = root level
    @Nullable
    private final ResourceLocation background;

    private static final int SIDE_MARGIN = 14;
    private static final int CRUMB_Y = 25;

    private Button saveButton;
    private SearchBoxWidget searchBox;
    private String searchQuery = "";

    public MoonlightConfigScreen(ModConfigHolder holder, ConfigCategory root, Screen returnScreen,
                                 @Nullable ResourceLocation background) {
        this(root, null, new ConfigEditSession(holder, returnScreen), background);
    }

    public static Screen create(ModConfigHolder holder, ConfigCategory root, Screen returnScreen,
                                @Nullable ResourceLocation background) {
        return new MoonlightConfigScreen(holder, root, returnScreen, background);
    }

    private MoonlightConfigScreen(ConfigCategory category, @Nullable MoonlightConfigScreen parentPage,
                                  ConfigEditSession session, @Nullable ResourceLocation background) {
        // the header keeps the config's own name on every sub-screen; the breadcrumb is what tracks the category
        super(session.holder().getReadableName());
        this.category = category;
        this.parentPage = parentPage;
        this.session = session;
        this.background = background;
    }

    private boolean isRoot() {
        return parentPage == null;
    }

    @Override
    public ConfigEditSession session() {
        return this.session;
    }

    @Override
    public void openCategory(ConfigCategory cat) {
        this.minecraft.setScreen(new MoonlightConfigScreen(cat, this, session, background));
    }

    @Override
    public void onValueEdited() {
        refreshSave();
    }

    @Override
    protected void init() {
        this.overlay.clear();
        this.list = new ConfigRowList(this.minecraft, this.width, this.height - HEADER - FOOTER, HEADER, ITEM_HEIGHT);

        this.searchBox = new SearchBoxWidget(this.font, this.width - SIDE_MARGIN - SearchBoxWidget.WIDTH, CRUMB_Y - 3,
                this.searchQuery, query -> {
            this.searchQuery = query;
            populate();
        });
        this.addRenderableWidget(this.searchBox);

        List<BreadcrumbWidget.Crumb> crumbs = new ArrayList<>();
        for (MoonlightConfigScreen s = this; s != null; s = s.parentPage) {
            Component label = s.isRoot() ? Component.literal("⌂") : s.category.title();
            crumbs.addFirst(new BreadcrumbWidget.Crumb(label, s, s == this));
        }
        int trailRight = this.searchBox.getX() - SearchBoxWidget.ICON_SPACE - 4; // leave room for the magnifier glyph + a gap
        BreadcrumbWidget breadcrumb = new BreadcrumbWidget(SIDE_MARGIN, CRUMB_Y, trailRight - SIDE_MARGIN, this.font.lineHeight,
                this.font, crumbs, target -> {
            if (target != this) this.minecraft.setScreen(target);
        });
        this.addRenderableWidget(breadcrumb);

        populate();
        this.addRenderableWidget(this.list);

        int y = this.height - 28;
        int bw = 100, gap = 4;
        if (isRoot()) {
            int total = 3 * bw + 2 * gap;
            int x0 = (this.width - total) / 2;
            this.addRenderableWidget(new IconButton(x0, y, bw, 20,
                    Component.translatable("gui.moonlight.config.reset_all"), MoonlightIcons.RESET, b -> confirmResetAll()));
            this.saveButton = new IconButton(x0 + bw + gap, y, bw, 20, Component.empty(), MoonlightIcons.SAVE, b -> doSave());
            this.addRenderableWidget(this.saveButton);
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(x0 + 2 * (bw + gap), y, bw, 20).build());
        } else {
            this.saveButton = new IconButton(this.width / 2 - 104, y, bw, 20, Component.empty(), MoonlightIcons.SAVE, b -> doSave());
            this.addRenderableWidget(this.saveButton);
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(this.width / 2 + 4, y, bw, 20).build());
        }
        this.addRenderableWidget(new GearButton(8, y, 20,
                b -> this.minecraft.setScreen(new ModsTilesScreen(this, background))));
        refreshSave();
    }

    private void confirmResetAll() {
        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                resetAllToDefaults(this.category);
                session.apply();
                session.clearPending();
            }
            this.minecraft.setScreen(this); // re-inits, so rows re-read the (now saved) values
        }, Component.translatable("gui.moonlight.config.reset_all.title"),
                Component.translatable("gui.moonlight.config.reset_all.message")));
    }

    private void resetAllToDefaults(ConfigCategory cat) {
        for (ConfigNode e : cat.entries()) {
            if (e instanceof ConfigCategory sub) {
                resetAllToDefaults(sub);
            } else if (e instanceof ConfigOption<?> v && !(v instanceof ConfigOption.UnsupportedValue)) {
                session.put(v, v.defaultValue());
            }
        }
    }

    @Override
    protected void populate() {
        List<ConfigListRow> rows = new ArrayList<>();
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            for (ConfigNode e : category.entries()) {
                if (e instanceof ConfigCategory cat) {
                    rows.add(new CategoryRow(this, cat));
                } else if (e instanceof ConfigOption<?> v) {
                    addOption(rows, v);
                }
            }
        } else {
            List<ConfigOption<?>> matches = new ArrayList<>();
            collectMatches(category, query, false, matches);
            for (ConfigOption<?> v : matches) {
                rows.add(new OptionRow(this, v, categorySearchPathOf(v)));
                addDescriptionRows(rows, v);
            }
        }
        this.list.setRows(rows);
    }

    private void addOption(List<ConfigListRow> rows, ConfigOption<?> v) {
        rows.add(new OptionRow(this, v));
        addDescriptionRows(rows, v);
    }

    private static void collectMatches(ConfigCategory category, String query, boolean inMatchedCategory,
                                       List<ConfigOption<?>> out) {
        for (ConfigNode e : category.entries()) {
            if (e instanceof ConfigCategory cat) {
                collectMatches(cat, query, inMatchedCategory || matches(cat.title(), query), out);
            } else if (e instanceof ConfigOption<?> v) {
                if (inMatchedCategory || (v != category.gate() && matches(v.title(), query))) out.add(v);
            }
        }
    }

    private static boolean matches(Component text, String query) {
        return text.getString().toLowerCase(Locale.ROOT).contains(query);
    }

    @Nullable
    private Component categorySearchPathOf(ConfigOption<?> option) {
        List<Component> parts = new ArrayList<>();
        for (ConfigCategory c = option.parent(); c != null && c != this.category; c = c.parent()) {
            parts.addFirst(c.title());
        }
        if (parts.isEmpty()) return null;
        MutableComponent path = Component.empty();
        for (Component part : parts) {
            path.append(part).append(BreadcrumbWidget.SEPARATOR);
        }
        return path.withStyle(s -> s.withColor(TextColor.fromRgb(ConfigGuiColors.CRUMB)));
    }

    private void doSave() {
        session.apply();
        session.clearPending();
        this.rebuildWidgets();
    }

    private void refreshSave() {
        if (this.saveButton == null) return;
        int unsaved = session.unsavedCount();
        Component count = Component.literal("(" + unsaved + ")")
                .withStyle(s -> s.withColor(TextColor.fromRgb(ConfigGuiColors.MODIFIED)));
        this.saveButton.setMessage(unsaved > 0
                ? Component.translatable("gui.moonlight.config.save_count", count)
                : Component.translatable("gui.moonlight.config.save"));
        this.saveButton.active = unsaved > 0;
    }

    @Override
    public void onClose() {
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
        else this.minecraft.setScreen(parentPage);
    }

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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        GuiHelper.renderHeaderBar(graphics, this.width, HEADER);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 7, ConfigGuiColors.TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderOverlayOrTooltip(graphics, mouseX, mouseY);
    }
}
