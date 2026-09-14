package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.AnchoredPopup;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

import static net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper.isMouseOver;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.widget.NumberPickerFieldWidget.twoDigits;

public class ClockPickerPopup extends AnchoredPopup {

    private enum Mode {HOUR, MINUTE}

    private static final int PAD = 4;
    private static final int FACE = 77;
    private static final int SLOT_W = 15;
    private static final int SLOT_H = 11;
    private static final int MODE_H = 12;

    private final NumberPickerFieldWidget field;
    private Mode mode = Mode.HOUR;

    public ClockPickerPopup(NumberPickerFieldWidget field) {
        super(field, PAD * 2 + FACE, PAD * 2 + FACE + 3 + MODE_H);
        this.field = field;
    }

    @Override
    protected void onOpened() {
        this.mode = Mode.HOUR;
    }

    private int modeTop(int y) {
        return y + PAD + FACE + 3;
    }

    private static int slotLeft(int centerX, int slot) {
        return centerX + (int) Math.round(Math.sin(Math.toRadians(slot * 30)) * 29) - SLOT_W / 2;
    }

    private static int slotTop(int centerY, int slot) {
        return centerY - (int) Math.round(Math.cos(Math.toRadians(slot * 30)) * 29) - SLOT_H / 2;
    }

    private static int slotAt(double mouseX, double mouseY, int centerX, int centerY) {
        for (int slot = 0; slot < 12; slot++) {
            if (isMouseOver(mouseX, mouseY, slotLeft(centerX, slot), slotTop(centerY, slot), SLOT_W, SLOT_H)) return slot;
        }
        return -1;
    }

    private String slotLabel(int slot) {
        if (mode == Mode.MINUTE) return twoDigits(slot * 5);
        return String.valueOf(slot == 0 ? 12 : slot);
    }

    // x of each piece of the "hh : mm AM" strip under the face
    private record TimeStripPos(int hourX, int colonX, int minuteX, int meridiemX) {
    }

    private static TimeStripPos modeStrip(Font font, int centerX) {
        int digitsW = font.width("00");
        int colonW = font.width(":");
        int amW = font.width("AM");
        int total = digitsW + 2 + colonW + 2 + digitsW + 6 + amW;
        int start = centerX - (total - 1) / 2;
        return new TimeStripPos(start, start + digitsW + 2, start + digitsW + 2 + colonW + 2, start + total - amW);
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        int hour = field.firstValue(), minute = field.secondValue();
        int centerX = x + width / 2, centerY = y + PAD + FACE / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MoonlightIcons.CLOCK_FACE, x + PAD, y + PAD, FACE, FACE);

        int handLength = 21;
        double handAngle = Math.toRadians(mode == Mode.HOUR ? (hour % 12) * 30 : minute * 6);
        GuiHelper.renderLine(graphics, centerX, centerY,
                centerX + (int) Math.round(Math.sin(handAngle) * handLength),
                centerY - (int) Math.round(Math.cos(handAngle) * handLength), TEXT);
        graphics.fill(centerX, centerY, centerX + 1, centerY + 1, TEXT);

        int selectedSlot = mode == Mode.HOUR ? hour % 12 : minute / 5;
        boolean onFiveMinuteMark = mode == Mode.HOUR || minute % 5 == 0;
        int hovered = slotAt(mouseX, mouseY, centerX, centerY);
        for (int slot = 0; slot < 12; slot++) {
            int sx = slotLeft(centerX, slot), sy = slotTop(centerY, slot);
            boolean selected = slot == selectedSlot && onFiveMinuteMark;
            if (slot == hovered) graphics.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, HOVER_HIGHLIGHT);
            GuiHelper.renderTextCenteredIn(graphics, font, slotLabel(slot), sx, sy, SLOT_W, SLOT_H, selected ? TEXT : DESCRIPTION);
        }

        int modeY = modeTop(y);
        int textY = modeY + (MODE_H - font.lineHeight) / 2 + 1;
        TimeStripPos strip = modeStrip(font, centerX);
        String hh = twoDigits(hour), mm = twoDigits(minute), meridiem = hour < 12 ? "AM" : "PM";
        graphics.text(font, hh, strip.hourX(), textY, mode == Mode.HOUR ? TEXT : DESCRIPTION, false);
        graphics.text(font, ":", strip.colonX(), textY, DESCRIPTION, false);
        graphics.text(font, mm, strip.minuteX(), textY, mode == Mode.MINUTE ? TEXT : DESCRIPTION, false);
        boolean overMeridiem = isMouseOver(mouseX, mouseY, strip.meridiemX(), modeY, font.width(meridiem), MODE_H);
        graphics.text(font, meridiem, strip.meridiemX(), textY, overMeridiem ? TEXT : DESCRIPTION, false);
        int underlineX = mode == Mode.HOUR ? strip.hourX() : strip.minuteX();
        graphics.fill(underlineX, modeY + MODE_H - 1, underlineX + font.width(hh), modeY + MODE_H, TEXT);
    }

    @Override
    protected void clickContent(double mouseX, double mouseY, int x, int y) {
        Font font = Minecraft.getInstance().font;
        int hour = field.firstValue(), minute = field.secondValue();
        int centerX = x + width / 2, centerY = y + PAD + FACE / 2;

        int slot = slotAt(mouseX, mouseY, centerX, centerY);
        if (slot >= 0) {
            GuiHelper.playClickSound();
            if (mode == Mode.HOUR) {
                field.setValues(slot + (hour < 12 ? 0 : 12), minute);
                this.mode = Mode.MINUTE;
            } else {
                field.setValues(hour, slot * 5);
                close();
            }
            return;
        }

        int modeY = modeTop(y);
        if (mouseY < modeY || mouseY >= modeY + MODE_H) return;
        TimeStripPos strip = modeStrip(font, centerX);
        int digits = font.width("00");
        if (mouseX >= strip.hourX() && mouseX < strip.hourX() + digits) {
            this.mode = Mode.HOUR;
        } else if (mouseX >= strip.minuteX() && mouseX < strip.minuteX() + digits) {
            this.mode = Mode.MINUTE;
        } else if (mouseX >= strip.meridiemX() && mouseX < strip.meridiemX() + font.width("AM")) {
            GuiHelper.playClickSound();
            field.setValues((hour + 12) % 24, minute);
        }
    }
}
