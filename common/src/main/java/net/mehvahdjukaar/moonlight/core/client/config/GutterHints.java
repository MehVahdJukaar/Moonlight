package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GutterHints {

    private static final int SIZE = 8;
    private static final int SPACING = SIZE + 3;

    private final List<Hint> hints = new ArrayList<>(2);
    private int rowY0, rowY1;
    private boolean drawIcons = true;

    public record Hint(int x, ResourceLocation icon, Component text) {
    }

    public void begin(int top, int height) {
        begin(top, height, true);
    }

    //with drawIcons off hints are only collected, for the description panel
    public void begin(int top, int height, boolean drawIcons) {
        this.hints.clear();
        this.rowY0 = top;
        this.rowY1 = top + height;
        this.drawIcons = drawIcons;
    }

    public void add(GuiGraphics graphics, int left, ResourceLocation icon, Component text) {
        int x = left - (this.hints.size() + 1) * SPACING;
        if (drawIcons) graphics.blitSprite(icon, x, this.rowY0 + (this.rowY1 - this.rowY0 - SIZE) / 2, SIZE, SIZE);
        this.hints.add(new Hint(x, icon, text));
    }

    public void addReload(GuiGraphics graphics, int left, ConfigReloadType type) {
        ResourceLocation icon = ConfigScreenLayout.reloadIcon(type);
        if (icon == null) return;
        add(graphics, left, icon, Component.translatable(type == ConfigReloadType.GAME_RESTART
                ? "gui.moonlight.config.reload.game" : "gui.moonlight.config.reload.world"));
    }


    public List<Hint> hints() {
        return hints;
    }

    @Nullable
    public Component tooltipAt(double mouseX, double mouseY) {
        if (mouseY < this.rowY0 || mouseY > this.rowY1) return null;
        for (Hint hint : this.hints) {
            if (mouseX >= hint.x() && mouseX <= hint.x() + SIZE) {
                return hint.text();
            }
        }
        return null;
    }
}
