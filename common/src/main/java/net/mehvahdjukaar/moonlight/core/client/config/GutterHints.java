package net.mehvahdjukaar.moonlight.core.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class GutterHints {

    private static final int SIZE = 8;
    private static final int SPACING = SIZE + 3;

    private final List<Hint> hints = new ArrayList<>(2);
    private int rowY0, rowY1;

    private record Hint(int x, Component tooltip) {
    }

    void begin(int top, int height) {
        this.hints.clear();
        this.rowY0 = top;
        this.rowY1 = top + height;
    }

    void add(GuiGraphics graphics, int left, ResourceLocation icon, Component tooltip) {
        int x = left - (this.hints.size() + 1) * SPACING;
        graphics.blitSprite(icon, x, this.rowY0 + (this.rowY1 - this.rowY0 - SIZE) / 2, SIZE, SIZE);
        this.hints.add(new Hint(x, tooltip));
    }

    @Nullable
    Component tooltipAt(double mouseX, double mouseY) {
        if (mouseY < this.rowY0 || mouseY > this.rowY1) return null;
        for (Hint hint : this.hints) {
            if (mouseX >= hint.x() && mouseX <= hint.x() + SIZE) return hint.tooltip();
        }
        return null;
    }
}
