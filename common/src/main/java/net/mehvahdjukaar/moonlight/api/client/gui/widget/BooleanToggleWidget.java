package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BooleanToggleWidget extends AbstractButton {

    private static final int ICON_SIZE = 12;
    private static final int ITEM_SIZE = 16; // decorative item drawn next to the symbol
    private static final int ITEM_GAP = 2;

    private final Identifier onIcon;
    private final Identifier offIcon;
    private boolean value;
    private final Consumer<Boolean> onChange;
    @Nullable
    private final ExtraIcon iconRenderer;

    public BooleanToggleWidget(int width, int height, Identifier onIcon, Identifier offIcon,
                               boolean initial, Consumer<Boolean> onChange) {
        this(width, height, onIcon, offIcon, initial, onChange, null);
    }

    public BooleanToggleWidget(int width, int height, Identifier onIcon, Identifier offIcon,
                               boolean initial, Consumer<Boolean> onChange, @Nullable ExtraIcon iconRenderer) {
        super(0, 0, width, height, Component.empty());
        this.onIcon = onIcon;
        this.offIcon = offIcon;
        this.value = initial;
        this.onChange = onChange;
        this.iconRenderer = iconRenderer;
    }

    public interface ExtraIcon {
        boolean available();

        void render(GuiGraphicsExtractor graphics, int x, int y, int size, boolean hovered, boolean lit);
    }

    public void set(boolean v) {
        this.value = v;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.value = !this.value;
        this.onChange.accept(this.value);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(graphics);
        int cy = getY() + getHeight() / 2;
        boolean hasIcon = iconRenderer != null && iconRenderer.available();
        // center the [item?][symbol] group as a whole so the symbol stays put when no icon is present
        int groupWidth = hasIcon ? ITEM_SIZE + ITEM_GAP + ICON_SIZE : ICON_SIZE;
        int x = getX() + (getWidth() - groupWidth) / 2;
        if (hasIcon) {
            iconRenderer.render(graphics, x, cy - ITEM_SIZE / 2, ITEM_SIZE, isHovered(), active);
            x += ITEM_SIZE + ITEM_GAP;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, value ? onIcon : offIcon, x, cy - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.USAGE,
                value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
