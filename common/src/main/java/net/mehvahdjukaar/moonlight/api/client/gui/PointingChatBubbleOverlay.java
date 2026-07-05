package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.ChatBubbleWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Renders a {@link ChatBubbleWidget} above a target widget each frame, without participating in layout or input.
 * <p>
 * Add to a screen via {@code screen::addRenderableOnly} when the bubble should follow another widget
 * and disappear when the supplied message is {@code null}.
 */
public class PointingChatBubbleOverlay implements Renderable {

    private final AbstractWidget target;
    private final IntSupplier screenWidth;
    private final Supplier<Component> messageSupplier;
    private final ChatBubbleWidget bubble;

    public PointingChatBubbleOverlay(AbstractWidget target, IntSupplier screenWidth,
                                     Supplier<Component> messageSupplier) {
        this(target, screenWidth, messageSupplier, true);
    }

    public PointingChatBubbleOverlay(AbstractWidget target, IntSupplier screenWidth,
                                     Supplier<Component> messageSupplier, boolean animated) {
        this.target = target;
        this.screenWidth = screenWidth;
        this.messageSupplier = messageSupplier;
        this.bubble = new ChatBubbleWidget(0, 0, Component.empty()).setAnimated(animated);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.target.visible) return;

        Component message = this.messageSupplier.get();
        if (message == null) return;

        if (!message.equals(this.bubble.getMessage())) {
            this.bubble.setText(message);
        }
        this.bubble.renderPointingAt(graphics, this.target, this.screenWidth.getAsInt(), mouseX, mouseY, partialTick);
    }
}
