package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An on/off button that draws a checkmark/cross sprite instead of ON/OFF text. The two sprites are supplied by the
 * caller, so the widget carries no dependency on any particular screen's assets. An optional {@link IconRenderer}
 * lets a caller draw a decorative item just to the left of the symbol (used by feature toggles), still without the
 * widget knowing how that icon is resolved or drawn.
 */
public class BooleanToggleWidget extends AbstractButton {

    private static final int ICON_SIZE = 12;
    private static final int ITEM_SIZE = 16; // decorative item drawn next to the symbol
    private static final int ITEM_GAP = 2;

    private final ResourceLocation onIcon;
    private final ResourceLocation offIcon;
    private boolean value;
    private final Consumer<Boolean> onChange;
    @Nullable
    private final IconRenderer iconRenderer;

    public BooleanToggleWidget(int width, int height, ResourceLocation onIcon, ResourceLocation offIcon,
                               boolean initial, Consumer<Boolean> onChange) {
        this(width, height, onIcon, offIcon, initial, onChange, null);
    }

    public BooleanToggleWidget(int width, int height, ResourceLocation onIcon, ResourceLocation offIcon,
                               boolean initial, Consumer<Boolean> onChange, @Nullable IconRenderer iconRenderer) {
        super(0, 0, width, height, Component.empty());
        this.onIcon = onIcon;
        this.offIcon = offIcon;
        this.value = initial;
        this.onChange = onChange;
        this.iconRenderer = iconRenderer;
    }

    /**
     * Draws the optional decorative icon shown next to the ✓/✗ symbol. {@link #available()} is checked first so the
     * widget can reserve room and center the icon+symbol group only when there really is an icon to draw.
     */
    public interface IconRenderer {
        boolean available();

        void render(GuiGraphics graphics, int x, int y, int size, boolean hovered, boolean lit);
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
        int cy = getY() + getHeight() / 2;
        boolean hasIcon = iconRenderer != null && iconRenderer.available();
        // center the [item?][symbol] group as a whole so the symbol stays put when no icon is present
        int groupWidth = hasIcon ? ITEM_SIZE + ITEM_GAP + ICON_SIZE : ICON_SIZE;
        int x = getX() + (getWidth() - groupWidth) / 2;
        if (hasIcon) {
            iconRenderer.render(graphics, x, cy - ITEM_SIZE / 2, ITEM_SIZE, isHovered(), active);
            x += ITEM_SIZE + ITEM_GAP;
        }
        graphics.blitSprite(value ? onIcon : offIcon, x, cy - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.USAGE,
                value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
