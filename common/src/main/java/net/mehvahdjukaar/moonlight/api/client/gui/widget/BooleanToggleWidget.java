package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * An on/off button that draws a checkmark/cross sprite instead of ON/OFF text. The two sprites are supplied by the
 * caller, so the widget carries no dependency on any particular screen's assets.
 */
public class BooleanToggleWidget extends AbstractButton {

    private static final int ICON_SIZE = 12;

    private final ResourceLocation onIcon;
    private final ResourceLocation offIcon;
    private boolean value;
    private final Consumer<Boolean> onChange;

    public BooleanToggleWidget(int width, int height, ResourceLocation onIcon, ResourceLocation offIcon,
                               boolean initial, Consumer<Boolean> onChange) {
        super(0, 0, width, height, Component.empty());
        this.onIcon = onIcon;
        this.offIcon = offIcon;
        this.value = initial;
        this.onChange = onChange;
    }

    public void set(boolean v) {
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
        graphics.blitSprite(value ? onIcon : offIcon, iconX, iconY, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.USAGE,
                value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
