package net.mehvahdjukaar.moonlight.core.client.config.schema;

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
import net.mehvahdjukaar.moonlight.core.client.config.CategoryRow;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigListRow;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigPageScreen;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigRowList;
import net.mehvahdjukaar.moonlight.core.client.config.OptionRow;
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

    private static final int ADD_ENTRY_STRIP = 24;

    private record FormEdit(ConfigEditSession session, SchemaForm.FormPart editedValue, Codec<?> codec, Consumer<Object> onDone) {}

    private final FormEdit edit;
    private final ConfigCategory category;
    @Nullable
    private final SchemaEditScreen parentPage;
    @Nullable
    private Button addButton;
    @Nullable
    private Component error;

    public static <T> Screen create(ConfigOption.SchemaValue<T> option, ConfigEditSession outerSession, Runnable onChange) {
        Screen parent = Minecraft.getInstance().screen;
        SchemaCodec<T> codec = option.codec;
        T current = outerSession.valueOrPendingValue(option);
        JsonElement currentJson = encode(codec, current);
        JsonElement defaultJson;
        try {
            defaultJson = encode(codec, option.defaultValue());
        } catch (Exception e) {
            defaultJson = currentJson;
        }
        SchemaForm form = SchemaForm.build(option.title(), codec.schema(), currentJson, defaultJson);
        Consumer<Object> onDone = decoded -> {
            outerSession.put(option, decoded);
            onChange.run();
        };
        FormEdit edit = new FormEdit(ConfigEditSession.scratch(parent), form.editedValue(), codec, onDone);
        return new SchemaEditScreen(form.rootPage(), null, edit, option.title());
    }

    private SchemaEditScreen(ConfigCategory category, @Nullable SchemaEditScreen parentPage, FormEdit edit, Component title) {
        super(title);
        this.category = category;
        this.parentPage = parentPage;
        this.edit = edit;
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
        return this.edit.session;
    }

    @Override
    public void openCategory(ConfigCategory cat) {
        this.minecraft.setScreen(new SchemaEditScreen(cat, this, edit, cat.title()));
    }

    @Override
    public void onValueEdited() {
        this.error = null;
        if (category instanceof SchemaCategory page && page.rebuildRowsIfSchemaChanged(edit.session)) {
            refreshRows();
        }
    }

    @Override
    protected void init() {
        this.overlay.clear();
        SchemaCategory.Entries<?> entries = entriesCategory();

        int footer = entries != null ? FOOTER + ADD_ENTRY_STRIP : FOOTER;
        this.list = new ConfigRowList(this.minecraft, this.width, this.height - HEADER - footer, HEADER, ITEM_HEIGHT);
        populate();
        this.addRenderableWidget(this.list);

        int y = this.height - 28;
        int cx = this.width / 2;
        if (entries != null) {
            Component label = Component.literal("+ ").withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable("gui.moonlight.config.list_add").withStyle(ChatFormatting.RESET));
            this.addButton = Button.builder(label, b -> addEntry(entries))
                    .bounds(cx - 100, y - ADD_ENTRY_STRIP, 200, 20).build();
            this.addButton.active = entries.canAdd();
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
    private SchemaCategory.Entries<?> entriesCategory() {
        return category instanceof SchemaCategory.Entries<?> entries ? entries : null;
    }

    @Override
    protected void populate() {
        SchemaCategory.Entries<?> entries = entriesCategory();
        List<ConfigListRow> rows = new ArrayList<>();
        List<ConfigNode> nodes = category.entries();
        for (int i = 0; i < nodes.size(); i++) {
            ConfigListRow row = nodes.get(i) instanceof ConfigCategory cat
                    ? new CategoryRow(this, cat)
                    : new OptionRow(this, (ConfigOption<?>) nodes.get(i));
            rows.add(entries == null ? row : removableEntryRow(entries, row, i));
        }
        this.list.setRows(rows);
    }

    private ConfigListRow removableEntryRow(SchemaCategory.Entries<?> entries, ConfigListRow row, int index) {
        if (entries instanceof SchemaCategory.MapEntries map) {
            row = new KeyValueRow(this, map.keyOption(index), row, () -> map.isDuplicateKey(edit.session, index));
        }
        return new ListEntryRow(row, entries.canRemove(), () -> removeEntry(entries, index));
    }

    private void addEntry(SchemaCategory.Entries<?> entries) {
        entries.addEntry(edit.session);
        refreshRows();
        this.list.setScrollAmount(this.list.maxScrollAmount());
    }

    private void removeEntry(SchemaCategory.Entries<?> entries, int index) {
        entries.removeEntry(edit.session, index);
        refreshRows();
    }

    private void refreshRows() {
        double scroll = this.list.scrollAmount();
        this.overlay.clear();
        populate();
        this.list.setScrollAmount(scroll);
        SchemaCategory.Entries<?> entries = entriesCategory();
        if (this.addButton != null && entries != null) this.addButton.active = entries.canAdd();
        this.error = null;
    }

    private void commit() {
        JsonElement json = edit.editedValue.toJson(edit.session);
        DataResult<?> result = edit.codec.parse(JsonOps.INSTANCE, json);
        var value = result.result();
        if (value.isPresent()) {
            edit.onDone.accept(value.get());
            this.minecraft.setScreen(edit.session.returnScreen());
        } else {
            this.error = Component.translatable("gui.moonlight.config.schema_invalid",
                    result.error().map(DataResult.Error::message).orElse(""));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(isRoot() ? edit.session.returnScreen() : parentPage);
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
            int y = this.height - 42 - (entriesCategory() != null ? ADD_ENTRY_STRIP : 0);
            graphics.centeredText(this.font, this.error, this.width / 2, y, ConfigGuiColors.ERROR);
        }
    }
}
