package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/**
 * Base class for the config controls made of several widgets (color field, range, vec3...). Focus between the inner
 * widgets goes through vanilla's setFocused(GuiEventListener). The problem is the boolean overload, which does
 * nothing on AbstractContainerWidget: when the row list drops the old row it calls setFocused(false) on us, and an
 * EditBox child would keep blinking its caret. So we pass the focus change down to the focused child. Safe, since
 * only real focus changes call that overload.
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
