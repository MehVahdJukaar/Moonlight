package net.mehvahdjukaar.moonlight.api.client.gui.screen;

import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.SyntaxEditBox;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.JsonHighlighter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A screen for editing a JSON string as pretty-printed, syntax-highlighted text in one big {@link SyntaxEditBox}.
 * On Done it hands the edited text back through {@code onApply} and returns to {@code parent}; Done is disabled
 * while the text isn't valid JSON, so nothing invalid is ever committed. Follows the same "edit on a sub page, hand
 * the result back on Done" shape as {@link ColorPickerScreen}.
 */
public class JsonEditScreen extends Screen {

    private static final int HEADER = 44;
    private static final int SIDE_MARGIN = 20;
    private static final int DESC_PAD_TOP = 6;
    private static final int DESC_PAD_BOTTOM = 8;

    private final Screen parent;
    private final Consumer<String> onApply;
    private final String initial;
    @Nullable
    private final Component description;

    private SyntaxEditBox editor;
    private Button done;
    private List<FormattedCharSequence> descriptionLines = List.of();
    private int descriptionBlockHeight;

    public JsonEditScreen(Component title, @Nullable Component description, String initial, Screen parent, Consumer<String> onApply) {
        super(title);
        this.description = description;
        this.initial = initial;
        this.parent = parent;
        this.onApply = onApply;
    }

    @Override
    protected void init() {
        layoutDescription();
        int top = HEADER + this.descriptionBlockHeight + 6;
        int bottom = this.height - 36;
        this.editor = new SyntaxEditBox(this.font, SIDE_MARGIN, top, this.width - 2 * SIDE_MARGIN, bottom - top,
                Component.translatable("gui.moonlight.config.json_hint"), JsonHighlighter.INSTANCE);
        this.editor.setValue(this.initial);
        this.editor.setValueListener(s -> refreshValid());
        this.addRenderableWidget(this.editor);

        int cx = this.width / 2;
        this.done = Button.builder(CommonComponents.GUI_DONE, b -> {
            onApply.accept(this.editor.getValue());
            onClose();
        }).bounds(cx - 100, this.height - 28, 96, 20).build();
        this.addRenderableWidget(this.done);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(cx + 4, this.height - 28, 96, 20).build());

        this.setInitialFocus(this.editor);
        refreshValid();
    }

    private void layoutDescription() {
        this.descriptionLines = List.of();
        this.descriptionBlockHeight = 0;
        if (this.description == null || this.description.getString().isBlank()) return;
        this.descriptionLines = this.font.split(this.description, this.width - 2 * SIDE_MARGIN);
        if (this.descriptionLines.isEmpty()) return;
        this.descriptionBlockHeight = DESC_PAD_TOP + this.descriptionLines.size() * this.font.lineHeight + DESC_PAD_BOTTOM;
    }

    private void refreshValid() {
        this.done.active = isValidJson(this.editor.getValue());
    }

    private static boolean isValidJson(String s) {
        try {
            JsonParser.parseString(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, HEADER, ConfigGuiColors.HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER - this.font.lineHeight) / 2, ConfigGuiColors.TITLE);
        renderDescription(graphics);
        if (!this.done.active) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.moonlight.config.json_invalid"),
                    this.width / 2, this.height - 42, ConfigGuiColors.ERROR);
        }
    }

    /** The config comment, word-wrapped under the title so it stays visible while editing. */
    private void renderDescription(GuiGraphics graphics) {
        if (this.descriptionLines.isEmpty()) return;
        int bandBottom = HEADER + this.descriptionBlockHeight;
        graphics.fill(0, HEADER, this.width, bandBottom, 0xFF121218);
        graphics.fill(0, bandBottom - 1, this.width, bandBottom, ConfigGuiColors.HEADER_SEPARATOR);
        int y = HEADER + DESC_PAD_TOP;
        for (FormattedCharSequence line : this.descriptionLines) {
            graphics.drawString(this.font, line, SIDE_MARGIN, y, ConfigGuiColors.DESCRIPTION);
            y += this.font.lineHeight;
        }
    }
}
