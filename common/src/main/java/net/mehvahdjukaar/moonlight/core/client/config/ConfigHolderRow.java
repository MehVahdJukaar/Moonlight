package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

class ConfigHolderRow extends ConfigListRow {

    private final Button button;
    private final Component label;
    @Nullable
    private final Component subtitle;
    private final Identifier icon;
    private final List<AbstractWidget> children;
    @Nullable
    private final Component unavailableReason;

    ConfigHolderRow(Component label, @Nullable Component subtitle, Identifier icon,
                    @Nullable Component unavailableReason, Runnable onClick) {
        this.label = label;
        this.subtitle = subtitle;
        this.icon = icon;
        this.unavailableReason = unavailableReason;
        this.button = Button.builder(Component.empty(), b -> onClick.run())
                .bounds(0, 0, ROW_WIDTH, ITEM_HEIGHT).build();
        this.button.active = unavailableReason == null;
        this.children = List.of(button);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int top = this.getContentY(), left = this.getX(), width = this.getWidth(), height = this.getContentHeight();
        Font font = Minecraft.getInstance().font;
        button.setMessage(Component.empty());
        button.setX(left);
        button.setWidth(width);
        button.setY(top);
        button.setHeight(height);
        button.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int iconX = left + 8;
        int textLeft = iconX + ROW_ICON + 6;
        int editX = left + width - 8 - ROW_ICON;
        int textRight = editX - GAP;

        boolean available = this.unavailableReason == null;
        int labelColor = available ? CATEGORY : DESCRIPTION;
        int subtitleColor = available ? TEXT : DISABLED;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, iconX, subtitle != null ? top + 5 : top + (height - ROW_ICON) / 2, ROW_ICON, ROW_ICON);
        if (subtitle != null) {
            GuiHelper.renderScrollingText(graphics, font, label, textLeft, textRight, top + 3, font.lineHeight + 2, labelColor);
            drawClipped(graphics, font, subtitle, textLeft, top + 5 + font.lineHeight, textRight, subtitleColor);
        } else {
            GuiHelper.renderScrollingText(graphics, font, label, textLeft, textRight, top, height, labelColor);
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, available ? MoonlightIcons.EDIT : MoonlightIcons.NO,
                editX, top + (height - ROW_ICON) / 2, ROW_ICON, ROW_ICON);
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
    Component getTooltip(int mouseX, int mouseY) {
        return unavailableReason;
    }
}
