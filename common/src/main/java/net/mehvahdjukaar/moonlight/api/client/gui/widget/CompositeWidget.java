package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/**
 * Shared base for the multi-widget config controls (color field, range, vec3...). Focus between the inner widgets is
 * driven by vanilla's {@code setFocused(GuiEventListener)} path. The one gap is the boolean overload, a no-op on
 * {@code AbstractContainerWidget}: when the row list clears the old row it reaches a composite as
 * {@code setFocused(false)}, and a bare {@code EditBox} child would keep its caret. Mirroring focus onto the focused
 * child closes that, and is safe since only genuine focus changes call the overload.
 */
public abstract class CompositeWidget extends AbstractContainerWidget {

    protected CompositeWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        GuiEventListener child = this.getFocused();
        if (child != null) child.setFocused(focused);
    }
}
