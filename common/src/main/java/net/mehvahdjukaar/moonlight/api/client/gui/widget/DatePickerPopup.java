package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.AnchoredPopup;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.Month;
import java.time.format.TextStyle;

import static net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper.isMouseOver;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

public class DatePickerPopup extends AnchoredPopup {

    private static final int PAD = 4;
    private static final int COLS = 7;
    private static final int CELL_W = 13;
    private static final int CELL_H = 11;
    private static final int HEADER_H = 14;

    private final NumberPickerFieldWidget field;

    public DatePickerPopup(NumberPickerFieldWidget field) {
        super(field, PAD * 2 + COLS * CELL_W, PAD * 2 + HEADER_H + 3 + 5 * CELL_H);
        this.field = field;
    }

    private Month month() {
        return Month.of(field.firstValue());
    }

    private int gridTop(int y) {
        return y + PAD + HEADER_H + 3;
    }

    private boolean overArrow(double mouseX, double mouseY, int x, int y, boolean next) {
        int arrowX = next ? x + width - PAD - CELL_W : x + PAD;
        return isMouseOver(mouseX, mouseY, arrowX, y + PAD, CELL_W, HEADER_H);
    }

    private int dayAt(double mouseX, double mouseY, int x, int y, Month month) {
        int gridX = x + PAD, gridY = gridTop(y);
        if (mouseX < gridX || mouseX >= gridX + COLS * CELL_W || mouseY < gridY) return 0;
        int col = (int) (mouseX - gridX) / CELL_W;
        int row = (int) (mouseY - gridY) / CELL_H;
        int day = row * COLS + col + 1;
        return day <= month.maxLength() ? day : 0;
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        Month month = month();
        int day = field.secondValue();
        int hovered = dayAt(mouseX, mouseY, x, y, month);

        int textY = y + PAD + (HEADER_H - font.lineHeight) / 2 + 1;
        boolean overPrev = overArrow(mouseX, mouseY, x, y, false), overNext = overArrow(mouseX, mouseY, x, y, true);
        graphics.centeredText(font, "<", x + PAD + CELL_W / 2, textY, overPrev ? TEXT : TEXT_SECONDARY);
        graphics.centeredText(font, ">", x + width - PAD - CELL_W / 2, textY, overNext ? TEXT : TEXT_SECONDARY);
        graphics.centeredText(font, month.getDisplayName(TextStyle.FULL, GuiHelper.currentLocale()), x + width / 2, textY, TEXT);

        int gridX = x + PAD, gridY = gridTop(y);
        for (int d = 1; d <= month.maxLength(); d++) {
            int cx = gridX + ((d - 1) % COLS) * CELL_W;
            int cy = gridY + ((d - 1) / COLS) * CELL_H;
            if (d == hovered) graphics.fill(cx, cy, cx + CELL_W, cy + CELL_H, HOVER_HIGHLIGHT);
            GuiHelper.renderTextCenteredIn(graphics, font, String.valueOf(d), cx, cy, CELL_W, CELL_H, d == day ? TEXT : TEXT_SECONDARY);
        }
    }

    @Override
    protected void clickContent(double mouseX, double mouseY, int x, int y) {
        Month month = month();
        int day = field.secondValue();
        if (overArrow(mouseX, mouseY, x, y, false)) {
            shiftMonth(month.minus(1), day);
        } else if (overArrow(mouseX, mouseY, x, y, true)) {
            shiftMonth(month.plus(1), day);
        } else {
            int picked = dayAt(mouseX, mouseY, x, y, month);
            if (picked == 0) return;
            GuiHelper.playClickSound();
            field.setValues(month.getValue(), picked);
            close();
        }
    }

    private void shiftMonth(Month month, int day) {
        GuiHelper.playClickSound();
        field.setValues(month.getValue(), Math.min(day, month.maxLength()));
    }
}
