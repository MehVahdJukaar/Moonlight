package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/**
 * An {@link AbstractContainerWidget} that actually relinquishes its children's focus.
 * <p>
 * Vanilla's {@code AbstractContainerWidget.setFocused(boolean)} delegates to a no-op default, so when a parent
 * unfocuses this composite through the boolean path — e.g. a selection list moving focus to another entry, which
 * calls {@code oldChild.setFocused(false)} — a focused inner widget (typically an {@link net.minecraft.client.gui.components.EditBox})
 * is never told it lost focus and keeps drawing its blinking caret / highlighted border. Clearing the focused
 * child here fixes that once for every composite control (color field, range, vec3, …).
 */
public abstract class CompositeWidget extends AbstractContainerWidget {

    protected CompositeWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            // drop the focused child (the GuiEventListener overload does propagate the unfocus, unlike the boolean one)
            this.setFocused((GuiEventListener) null);
        }
    }
}
