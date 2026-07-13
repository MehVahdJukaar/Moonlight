package net.mehvahdjukaar.moonlight.core.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

/**
 * A schema-driven form editor for a single codec-backed config value ({@link ConfigOption.SchemaValue}). A CodecUI
 * {@link net.mehvahdjukaar.codecui.Schema} is converted (by {@link SchemaForm}) into the same
 * {@link ConfigCategory}/{@link ConfigOption} tree the main config screen renders, and this screen drives it with the
 * exact same rows ({@code OptionRow}/{@code CategoryRow}), controls ({@link ConfigControllers}) and edit session — so a
 * generated form looks and behaves identically to a hand-written config page, with no bespoke widget code.
 *
 * <p>All working edits live in a private {@link ConfigEditSession} (holder-less: nothing here writes to disk) shared
 * across the sub-category navigation stack. Only the root page commits: on <em>Done</em> the form's JSON is reassembled
 * from the session, decoded through the codec, and — if valid — handed back to the outer config screen; sub-record
 * pages just navigate. Follows the "edit on a sub page, hand the result back on Done" shape of {@code JsonEditScreen}.</p>
 */
public class SchemaEditScreen extends Screen implements ConfigScreenAccess, PopupHost {

    /** Shared state across the whole sub-category navigation stack of one editing visit. */
    private record State(ConfigEditSession session, SchemaForm.Reader reader, Codec<?> codec, Consumer<Object> onDone) {}

    private final State state;
    private final ConfigCategory category;
    @Nullable
    private final SchemaEditScreen parentPage; // null = root page (the one that commits)
    private final OverlayLayer overlay = new OverlayLayer();

    private ConfigOptionList list;
    @Nullable
    private Component error;

    /**
     * Opens the editor for a schema-backed config value. The current working value comes from {@code outerSession}; on
     * Done the decoded object is staged back into it (and {@code onChange} fired), exactly like any other control.
     */
    public static <T> Screen create(ConfigOption.SchemaValue<T> option, ConfigEditSession outerSession, Runnable onChange) {
        Screen parent = Minecraft.getInstance().screen;
        SchemaCodec<T> codec = option.codec;
        T current = outerSession.current(option);
        JsonElement currentJson = encode(codec, current);
        JsonElement defaultJson;
        try {
            defaultJson = encode(codec, option.defaultValue());
        } catch (Exception e) {
            defaultJson = currentJson; // default may reference things not available; seeding from current is fine
        }
        SchemaForm form = SchemaForm.build(option.title(), codec.schema(), currentJson, defaultJson);
        Consumer<Object> onDone = decoded -> {
            outerSession.put(option, decoded);
            onChange.run();
        };
        State state = new State(new ConfigEditSession(null, parent), form.reader, codec, onDone);
        return new SchemaEditScreen(form.root, null, state, option.title());
    }

    private SchemaEditScreen(ConfigCategory category, @Nullable SchemaEditScreen parentPage, State state, Component title) {
        super(title);
        this.category = category;
        this.parentPage = parentPage;
        this.state = state;
    }

    private static <T> JsonElement encode(Codec<T> codec, @Nullable T value) {
        if (value == null) return new JsonObject();
        return codec.encodeStart(JsonOps.INSTANCE, value).result().orElseGet(JsonObject::new);
    }

    private boolean isRoot() {
        return parentPage == null;
    }

    // ===== ConfigScreenAccess =====

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public ConfigEditSession session() {
        return this.state.session;
    }

    @Override
    public void openCategory(ConfigCategory cat) {
        this.minecraft.setScreen(new SchemaEditScreen(cat, this, state, cat.title()));
    }

    @Override
    public void toggleExpanded(ConfigOption<?> value) {
        state.session.toggleExpanded(value);
        populate();
    }

    @Override
    public void onValueEdited() {
        this.error = null; // a fresh edit may well have fixed whatever was invalid; re-checked on Done
    }

    @Override
    public boolean isCategoryEnabled(ConfigCategory cat) {
        ConfigOption.BooleanValue gate = cat.gate();
        boolean own = gate == null || Boolean.TRUE.equals(state.session.current(gate));
        ConfigCategory parent = cat.parent();
        return own && (parent == null || isCategoryEnabled(parent));
    }

    // ===== PopupHost =====

    @Override
    public OverlayLayer getOverlayLayer() {
        return this.overlay;
    }

    // ===== screen =====

    @Override
    protected void init() {
        this.overlay.clear();
        this.list = new ConfigOptionList(this.minecraft, this.width, this.height - HEADER - FOOTER, HEADER, ITEM_HEIGHT);
        populate();
        this.addRenderableWidget(this.list);

        int y = this.height - 28;
        int cx = this.width / 2;
        if (isRoot()) {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> commit())
                    .bounds(cx - 100, y, 96, 20).build());
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                    .bounds(cx + 4, y, 96, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(cx - 50, y, 100, 20).build());
        }
    }

    private void populate() {
        List<ConfigListRow> rows = new ArrayList<>();
        for (ConfigNode e : category.entries()) {
            if (e instanceof ConfigCategory cat) {
                rows.add(new CategoryRow(this, cat));
            } else if (e instanceof ConfigOption<?> v) {
                rows.add(new OptionRow(this, v));
                if (v.description() != null && state.session.isExpanded(v)) {
                    List<FormattedCharSequence> lines = this.font.split(v.description(), ROW_WIDTH - ARROW_WIDTH - GAP);
                    for (int i = 0; i < lines.size(); i += DESC_LINES_PER_ROW) {
                        rows.add(new DescriptionRow(this.font, lines.subList(i, Math.min(i + DESC_LINES_PER_ROW, lines.size()))));
                    }
                }
            }
        }
        this.list.setRows(rows);
    }

    /** Reassembles the form's JSON, decodes it through the codec and, if valid, hands the value back and closes. */
    private void commit() {
        JsonElement json = state.reader.read(state.session);
        DataResult<?> result = state.codec.parse(JsonOps.INSTANCE, json);
        var value = result.result();
        if (value.isPresent()) {
            state.onDone.accept(value.get());
            this.minecraft.setScreen(state.session.returnScreen());
        } else {
            this.error = Component.translatable("gui.moonlight.config.schema_invalid",
                    result.error().map(DataResult.Error::message).orElse(""));
        }
    }

    @Override
    public void onClose() {
        // sub-page: go back up (edits stay in the shared session). root: leave without committing (cancel).
        this.minecraft.setScreen(isRoot() ? state.session.returnScreen() : parentPage);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return overlay.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return overlay.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return overlay.keyPressed(key, scanCode, modifiers) || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return overlay.charTyped(c, modifiers) || super.charTyped(c, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, HEADER, ConfigGuiColors.HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER - this.font.lineHeight) / 2, ConfigGuiColors.TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (overlay.isOpen()) {
            overlay.render(graphics, mouseX, mouseY);
            return;
        }
        ConfigListRow hovered = this.list.getHovered(mouseX, mouseY);
        Component tooltip = hovered != null ? hovered.getTooltip(mouseX, mouseY) : null;
        if (tooltip == null) {
            for (ConfigListRow row : this.list.children()) {
                tooltip = row.getGutterTooltip(mouseX, mouseY);
                if (tooltip != null) break;
            }
        }
        if (tooltip != null) {
            graphics.renderTooltip(this.font, this.font.split(tooltip, 220), mouseX, mouseY);
        }
        if (this.error != null) {
            graphics.drawCenteredString(this.font, this.error, this.width / 2, this.height - 42, ConfigGuiColors.ERROR);
        }
    }
}
