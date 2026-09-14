package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

class ConfigHolderRow extends ConfigListRow {

    private final Button button;
    private final Component label;
    @Nullable
    private final Component subtitle;
    private final ResourceLocation icon;
    private final List<AbstractWidget> children;
    @Nullable
    private final Component unavailableReason;

    ConfigHolderRow(Component label, @Nullable Component subtitle, ResourceLocation icon,
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
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        Font font = Minecraft.getInstance().font;
        button.setMessage(Component.empty());
        button.setX(left);
        button.setWidth(width);
        button.setY(top);
        button.setHeight(height);
        button.render(graphics, mouseX, mouseY, partialTick);

        int iconX = left + 8;
        int textLeft = iconX + ROW_ICON + 6;
        int editX = left + width - 8 - ROW_ICON;
        int textRight = editX - GAP;

        boolean available = this.unavailableReason == null;
        int labelColor = available ? CATEGORY : DESCRIPTION;
        int subtitleColor = available ? TEXT : DISABLED;

        graphics.blitSprite(icon, iconX, subtitle != null ? top + 5 : top + (height - ROW_ICON) / 2, ROW_ICON, ROW_ICON);
        if (subtitle != null) {
            GuiHelper.renderScrollingText(graphics, font, label, textLeft, textRight, top + 3, font.lineHeight + 2, labelColor);
            drawClipped(graphics, font, subtitle, textLeft, top + 5 + font.lineHeight, textRight, subtitleColor);
        } else {
            GuiHelper.renderScrollingText(graphics, font, label, textLeft, textRight, top, height, labelColor);
        }
        graphics.blitSprite(available ? MoonlightIcons.EDIT : MoonlightIcons.NO,
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
