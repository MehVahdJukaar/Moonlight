package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

/**
 * A {@link MultiLineEditBox} that renders its text through a per-line {@link SyntaxHighlighter} (vanilla only draws
 * each line as a flat-colored plain string). Supply any highlighter — e.g. {@link JsonHighlighter#INSTANCE} — and
 * this re-renders each display line highlighted; the text model, cursor, selection and scrolling all stay in the
 * inherited {@link MultilineTextField} (reached via the {@code textField} access widener). Only rendering changes.
 */
public class SyntaxEditBox extends MultiLineEditBox {

    // shared palette, forced opaque (these are drawn with an explicit alpha)
    private static final int TEXT_COLOR = 0xFF000000 | ConfigGuiColors.TEXT;
    private static final int PLACEHOLDER_COLOR = 0xFF000000 | ConfigGuiColors.DESCRIPTION;
    private static final int CURSOR_COLOR = 0xFF000000 | ConfigGuiColors.TEXT;
    private static final int SELECTION_COLOR = ConfigGuiColors.SELECTION_BG;

    private final Font font;
    private final Component placeholder;
    private final SyntaxHighlighter highlighter;
    private long focusedTime = Util.getMillis();

    public SyntaxEditBox(Font font, int x, int y, int width, int height, Component placeholder,
                         SyntaxHighlighter highlighter) {
        super(font, x, y, width, height, placeholder, placeholder);
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
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String value = this.textField.value();
        if (value.isEmpty() && !this.isFocused()) {
            graphics.drawWordWrap(this.font, this.placeholder, this.getX() + this.innerPadding(),
                    this.getY() + this.innerPadding(), this.width - this.totalInnerPadding(), PLACEHOLDER_COLOR);
            return;
        }

        int lineHeight = this.font.lineHeight;
        int textX = this.getX() + this.innerPadding();
        int y = this.getY() + this.innerPadding();

        renderSelection(graphics, value, textX, lineHeight);

        int cursor = this.textField.cursor();
        boolean showCursor = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
        int cursorX = textX;
        int cursorY = y;
        boolean placedCursor = false;

        for (MultilineTextField.StringView line : this.textField.iterateLines()) {
            if (this.withinContentAreaTopBottom(y, y + lineHeight)) {
                String lineText = value.substring(line.beginIndex(), line.endIndex());
                graphics.drawString(this.font, this.highlighter.highlightLine(lineText), textX, y, TEXT_COLOR);
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
                graphics.drawString(this.font, "_", cursorX, cursorY, CURSOR_COLOR);
            } else {
                graphics.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + lineHeight, CURSOR_COLOR);
            }
        }
    }

    private void renderSelection(GuiGraphics graphics, String value, int textX, int lineHeight) {
        if (!this.textField.hasSelection()) return;
        MultilineTextField.StringView selection = this.textField.getSelected();
        int y = this.getY() + this.innerPadding();
        for (MultilineTextField.StringView line : this.textField.iterateLines()) {
            if (selection.beginIndex() <= line.endIndex()) {
                if (line.beginIndex() > selection.endIndex()) break;
                if (this.withinContentAreaTopBottom(y, y + lineHeight)) {
                    int from = this.font.width(value.substring(line.beginIndex(),
                            Math.max(selection.beginIndex(), line.beginIndex())));
                    int to = selection.endIndex() > line.endIndex()
                            ? this.width - this.innerPadding()
                            : this.font.width(value.substring(line.beginIndex(), selection.endIndex()));
                    graphics.fill(RenderType.guiTextHighlight(), textX + from, y, textX + to, y + lineHeight, SELECTION_COLOR);
                }
            }
            y += lineHeight;
        }
    }
}
