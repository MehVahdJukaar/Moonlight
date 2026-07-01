package net.mehvahdjukaar.moonlight.api.client.gui;

import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A screen for editing a JSON string as pretty-printed, syntax-highlighted text in one big {@link SyntaxEditBox}.
 * On Done it hands the edited text back through {@code onApply} and returns to {@code parent}; Done is disabled
 * while the text isn't valid JSON, so nothing invalid is ever committed. Follows the same "edit on a sub page, hand
 * the result back on Done" shape as {@link ColorPickerScreen}.
 */
public class JsonEditScreen extends Screen {

    private static final int HEADER = 44;
    private static final int HEADER_BG = ConfigGuiColors.HEADER_BG;
    private static final int HEADER_SEPARATOR = ConfigGuiColors.HEADER_SEPARATOR;
    private static final int TITLE_COLOR = ConfigGuiColors.TITLE;
    private static final int ERROR_COLOR = ConfigGuiColors.ERROR;

    private final Screen parent;
    private final Consumer<String> onApply;
    private final String initial;

    private SyntaxEditBox editor;
    private Button done;

    public JsonEditScreen(Component title, String initial, Screen parent, Consumer<String> onApply) {
        super(title);
        this.initial = initial;
        this.parent = parent;
        this.onApply = onApply;
    }

    @Override
    protected void init() {
        int margin = 20;
        int top = HEADER + 6;
        int bottom = this.height - 36;
        this.editor = new SyntaxEditBox(this.font, margin, top, this.width - 2 * margin, bottom - top,
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
        graphics.fill(0, 0, this.width, HEADER, HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER - this.font.lineHeight) / 2, TITLE_COLOR);
        if (!this.done.active) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.moonlight.config.json_invalid"),
                    this.width / 2, this.height - 42, ERROR_COLOR);
        }
    }
}
