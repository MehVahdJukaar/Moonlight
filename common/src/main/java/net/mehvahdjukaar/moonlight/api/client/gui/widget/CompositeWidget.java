package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/**
 * Shared base for the multi-widget config controls (color field, range, vec3, …).
 * <p>
 * Focus <em>between</em> the inner widgets is driven entirely by vanilla's {@code setFocused(GuiEventListener)} path,
 * so nothing is micromanaged there. The one gap is the boolean overload: vanilla's
 * {@code AbstractContainerWidget.setFocused(boolean)} is a no-op, so when the row list switches rows and clears the
 * old row via {@code oldRow.setFocused((GuiEventListener) null)} — which reaches this composite as a boolean
 * {@code setFocused(false)} — a bare {@link net.minecraft.client.gui.components.EditBox} child would keep its caret.
 * Mirroring our focus onto the currently focused child (as a leaf widget would) closes that gap. It is safe because
 * the only callers of this boolean overload are genuine focus changes: the list is idempotent (vanilla guards
 * {@code getFocused() != focused}) so no spurious {@code false} arrives, and the same-row redundant
 * {@code false}→{@code true} round-trip just toggles the same child off and back on.
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
