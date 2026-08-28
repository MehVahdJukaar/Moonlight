package net.mehvahdjukaar.moonlight.core.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

public class SchemaEditScreen extends ConfigPageScreen {

    // shared across the whole sub-category navigation stack of one editing visit
    private record State(ConfigEditSession session, SchemaForm.Reader reader, Codec<?> codec, Consumer<Object> onDone) {}

    private final State state;
    private final ConfigCategory category;
    @Nullable
    private final SchemaEditScreen parentPage; // null = root page (the one that commits)

    @Nullable
    private Button addButton; // list pages only; kept to re-evaluate its enabled state after an entry is added
    @Nullable
    private Component error;

    // the current working value comes from outerSession; on Done the decoded object is staged back into it
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
        State state = new State(ConfigEditSession.scratch(parent), form.reader, codec, onDone);
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

    @Override
    public ConfigEditSession session() {
        return this.state.session;
    }

    @Override
    public void openCategory(ConfigCategory cat) {
        this.minecraft.setScreen(new SchemaEditScreen(cat, this, state, cat.title()));
    }

    @Override
    public void onValueEdited() {
        this.error = null; // a fresh edit may well have fixed whatever was invalid; re-checked on Done
    }

    @Override
    protected void init() {
        this.overlay.clear();
        SchemaForm.ListCategory listCategory = listCategory();

        int footer = listCategory != null ? FOOTER + 24 : FOOTER;
        this.list = new ConfigRowList(this.minecraft, this.width, this.height - HEADER - footer, HEADER, ITEM_HEIGHT);
        populate();
        this.addRenderableWidget(this.list);

        int y = this.height - 28;
        int cx = this.width / 2;
        if (listCategory != null) {
            Component label = Component.literal("+ ").withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable("gui.moonlight.config.list_add").withStyle(ChatFormatting.RESET));
            this.addButton = Button.builder(label, b -> addEntry(listCategory))
                    .bounds(cx - 100, y - 24, 200, 20).build();
            this.addButton.active = listCategory.canAdd();
            this.addRenderableWidget(this.addButton);
        }
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

    @Nullable
    private SchemaForm.ListCategory listCategory() {
        return category instanceof SchemaForm.ListCategory lc ? lc : null;
    }

    @Override
    protected void populate() {
        SchemaForm.ListCategory listCategory = listCategory();
        List<ConfigListRow> rows = new ArrayList<>();
        List<ConfigNode> entries = category.entries();
        for (int i = 0; i < entries.size(); i++) {
            ConfigNode e = entries.get(i);
            ConfigListRow row;
            if (e instanceof ConfigCategory cat) {
                row = new CategoryRow(this, cat);
            } else if (e instanceof ConfigOption<?> v) {
                row = new OptionRow(this, v);
            } else {
                continue;
            }
            if (listCategory != null) {
                int index = i;
                rows.add(new ListEntryRow(row, listCategory.canRemove(), () -> removeEntry(listCategory, index)));
                continue; // generated list entries never carry a description, so there is nothing to expand
            }
            rows.add(row);
            if (e instanceof ConfigOption<?> v) addDescriptionRows(rows, v);
        }
        this.list.setRows(rows);
    }

    private void addEntry(SchemaForm.ListCategory cat) {
        List<JsonElement> values = cat.snapshot(state.session);
        values.add(cat.newEntry());
        rebuild(cat, values);
        this.list.setScrollAmount(this.list.maxScrollAmount()); // reveal the entry that was just appended
    }

    private void removeEntry(SchemaForm.ListCategory cat, int index) {
        List<JsonElement> values = cat.snapshot(state.session);
        if (index >= values.size()) return;
        values.remove(index);
        rebuild(cat, values);
    }

    private void rebuild(SchemaForm.ListCategory cat, List<JsonElement> values) {
        double scroll = this.list.scrollAmount();
        cat.setEntries(values);
        this.overlay.clear(); // the rows are recreated below, and with them any popup one of them had open
        populate();
        this.list.setScrollAmount(scroll);
        if (this.addButton != null) this.addButton.active = cat.canAdd();
        onValueEdited();
    }

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
        // sub-page: go back up, edits stay in the shared session. Root: leave without committing
        this.minecraft.setScreen(isRoot() ? state.session.returnScreen() : parentPage);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (renderOverlayOrTooltip(graphics, mouseX, mouseY)) return;
        if (this.error != null) {
            // sits just above the button strip, which is one row taller on a list page (the "add entry" button)
            int y = this.height - (listCategory() != null ? 66 : 42);
            graphics.centeredText(this.font, this.error, this.width / 2, y, ConfigGuiColors.ERROR);
        }
    }
}
