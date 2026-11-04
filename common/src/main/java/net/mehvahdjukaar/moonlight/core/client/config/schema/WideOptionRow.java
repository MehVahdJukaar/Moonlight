package net.mehvahdjukaar.moonlight.core.client.config.schema;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigControl;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigControllers;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigRow;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WideOptionRow extends ConfigRow {
    private final ConfigControl<?> control;
    private final List<AbstractWidget> children;

    public WideOptionRow(ConfigScreenAccess view, ConfigOption<?> option, int width) {
        this.control = ConfigControllers.create(option, view.session(), view::onValueEdited, width);
        this.children = List.of(control.widget());
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        AbstractWidget w = control.widget();
        w.setX(this.getX());
        w.setY(this.getContentY() + (this.getContentHeight() - w.getHeight()) / 2);
        w.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Nullable
    @Override
    public Component getTooltip(int mouseX, int mouseY) {
        return null;
    }
}
