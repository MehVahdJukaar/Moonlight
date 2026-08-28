package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/** Base for the multi-widget config controls (color field, range, vec3). */
public abstract class CompositeWidget extends AbstractContainerWidget {

    protected CompositeWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message, AbstractScrollArea.defaultSettings(0));
    }

    @Override
    protected int contentHeight() {
        return this.getHeight();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        GuiEventListener child = this.getFocused();
        if (child != null) child.setFocused(focused);
    }
}
