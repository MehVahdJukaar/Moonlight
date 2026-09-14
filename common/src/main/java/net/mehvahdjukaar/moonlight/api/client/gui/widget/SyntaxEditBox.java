package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.SyntaxHighlighter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.TextCursorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/**
 * A MultiLineEditBox that draws its text through a SyntaxHighlighter, one line at a time, where vanilla just draws
 * each line in a single color. Only the drawing changes: the text itself, the cursor, the selection and the
 * scrolling all stay in the inherited MultilineTextField.
 */
public class SyntaxEditBox extends MultiLineEditBox {

    private final Font font;
    private final Component placeholder;
    private final SyntaxHighlighter highlighter;
    private long focusedTime = Util.getMillis();

    public SyntaxEditBox(Font font, int x, int y, int width, int height, Component placeholder,
                         SyntaxHighlighter highlighter) {
        super(font, x, y, width, height, placeholder, placeholder, ConfigGuiColors.TEXT, true, ConfigGuiColors.TEXT, true, true);
        this.font = font;
        this.placeholder = placeholder;
        this.highlighter = highlighter;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) this.focusedTime = Util.getMillis();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        String value = this.textField.value();
        if (value.isEmpty() && !this.isFocused()) {
            graphics.textWithWordWrap(this.font, this.placeholder, this.getInnerLeft(), this.getInnerTop(),
                    this.width - this.totalInnerPadding(), ConfigGuiColors.DESCRIPTION);
            return;
        }

        int lineHeight = this.font.lineHeight;
        int textX = this.getInnerLeft();
        int y = this.getInnerTop();

        extractSelection(graphics, value, textX, lineHeight);

        int cursor = this.textField.cursor();
        boolean showCursor = this.isFocused() && TextCursorUtils.isCursorVisible(Util.getMillis() - this.focusedTime);
        int cursorX = textX;
        int cursorY = y;
        boolean placedCursor = false;

        for (MultilineTextField.StringView line : this.textField.iterateLines()) {
            if (this.withinContentAreaTopBottom(y, y + lineHeight)) {
                String lineText = value.substring(line.beginIndex(), line.endIndex());
                graphics.text(this.font, this.highlighter.highlightLine(lineText), textX, y, ConfigGuiColors.TEXT);
            }
            if (!placedCursor && cursor >= line.beginIndex() && cursor <= line.endIndex()) {
                cursorX = textX + this.font.width(value.substring(line.beginIndex(), cursor));
                cursorY = y;
                placedCursor = true;
            }
            y += lineHeight;
        }

        if (showCursor && placedCursor && this.withinContentAreaTopBottom(cursorY, cursorY + lineHeight)) {
            if (cursor >= value.length()) {
                TextCursorUtils.extractAppendCursor(graphics, this.font, cursorX, cursorY, ConfigGuiColors.TEXT, true);
            } else {
                TextCursorUtils.extractInsertCursor(graphics, cursorX, cursorY, ConfigGuiColors.TEXT, lineHeight);
            }
        }
    }

    private void extractSelection(GuiGraphicsExtractor graphics, String value, int textX, int lineHeight) {
        if (!this.textField.hasSelection()) return;
        MultilineTextField.StringView selection = this.textField.getSelected();
        int y = this.getInnerTop();
        for (MultilineTextField.StringView line : this.textField.iterateLines()) {
            if (selection.beginIndex() <= line.endIndex()) {
                if (line.beginIndex() > selection.endIndex()) break;
                if (this.withinContentAreaTopBottom(y, y + lineHeight)) {
                    int from = this.font.width(value.substring(line.beginIndex(),
                            Math.max(selection.beginIndex(), line.beginIndex())));
                    int to = selection.endIndex() > line.endIndex()
                            ? this.width - this.innerPadding()
                            : this.font.width(value.substring(line.beginIndex(), selection.endIndex()));
                    graphics.textHighlight(textX + from, y, textX + to, y + lineHeight, true);
                }
            }
            y += lineHeight;
        }
    }
}
