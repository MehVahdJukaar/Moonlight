package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.AnchoredPopup;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

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

    private static int[] modeLayout(Font font, int centerX) {
        int digits = font.width("00"), colon = font.width(":"), meridiem = font.width("AM");
        int total = digits + 2 + colon + 2 + digits + 6 + meridiem;
        int start = centerX - (total - 1) / 2;
        return new int[]{start, start + digits + 2, start + digits + 2 + colon + 2, start + total - meridiem};
    }

    @Override
    protected void renderContent(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        int hour = field.firstValue(), minute = field.secondValue();
        int centerX = x + width / 2, centerY = y + PAD + FACE / 2;
        graphics.blitSprite(MoonlightIcons.CLOCK_FACE, x + PAD, y + PAD, FACE, FACE);

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
            GuiHelper.renderTextCenteredIn(graphics, font, slotLabel(slot), sx, sy, SLOT_W, SLOT_H, selected ? TEXT : TEXT_SECONDARY);
        }

        int modeY = modeTop(y);
        int textY = modeY + (MODE_H - font.lineHeight) / 2 + 1;
        int[] parts = modeLayout(font, centerX);
        String hh = twoDigits(hour), mm = twoDigits(minute), meridiem = hour < 12 ? "AM" : "PM";
        graphics.drawString(font, hh, parts[0], textY, mode == Mode.HOUR ? TEXT : TEXT_SECONDARY, false);
        graphics.drawString(font, ":", parts[1], textY, TEXT_SECONDARY, false);
        graphics.drawString(font, mm, parts[2], textY, mode == Mode.MINUTE ? TEXT : TEXT_SECONDARY, false);
        boolean overMeridiem = isMouseOver(mouseX, mouseY, parts[3], modeY, font.width(meridiem), MODE_H);
        graphics.drawString(font, meridiem, parts[3], textY, overMeridiem ? TEXT : TEXT_SECONDARY, false);
        int underlineX = mode == Mode.HOUR ? parts[0] : parts[2];
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
        int[] parts = modeLayout(font, centerX);
        int digits = font.width("00");
        if (mouseX >= parts[0] && mouseX < parts[0] + digits) {
            this.mode = Mode.HOUR;
        } else if (mouseX >= parts[2] && mouseX < parts[2] + digits) {
            this.mode = Mode.MINUTE;
        } else if (mouseX >= parts[3] && mouseX < parts[3] + font.width("AM")) {
            GuiHelper.playClickSound();
            field.setValues((hour + 12) % 24, minute);
        }
    }
}
