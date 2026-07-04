package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

class BooleanToggleWidget extends AbstractButton {

    private static final int ICON_SIZE = 12;

    private boolean value;
    private final Consumer<Boolean> onChange;

    BooleanToggleWidget(int width, int height, boolean initial, Consumer<Boolean> onChange) {
        super(0, 0, width, height, Component.empty());
        this.value = initial;
        this.onChange = onChange;
    }

    void set(boolean v) {
        this.value = v;
    }

    @Override
    public void onPress() {
        this.value = !this.value;
        this.onChange.accept(this.value);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int iconX = getX() + (getWidth() - ICON_SIZE) / 2;
        int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
        graphics.blitSprite(value ? ON_ICON : OFF_ICON, iconX, iconY, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.USAGE,
                value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
