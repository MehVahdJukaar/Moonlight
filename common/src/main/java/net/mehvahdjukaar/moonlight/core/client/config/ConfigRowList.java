package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.ROW_WIDTH;

class ConfigRowList extends ContainerObjectSelectionList<ConfigListRow> {

    private boolean drawFooterSeparator = true;
    private int rowWidth = ROW_WIDTH;

    ConfigRowList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    void setRows(List<ConfigListRow> rows) {
        this.clearEntries();
        for (ConfigListRow row : rows) this.addEntry(row);
        this.clampScrollAmount();
    }

    @Nullable
    ConfigListRow getHovered(double mouseX, double mouseY) {
        return this.getEntryAtPosition(mouseX, mouseY);
    }

    @Override
    public int getRowWidth() {
        return this.rowWidth;
    }

    // narrows the rows, for lists that live in a pane instead of the whole screen
    void setRowWidth(int rowWidth) {
        this.rowWidth = rowWidth;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width / 2 + this.getRowWidth() / 2 + 6;
    }

    // Blank space above the first row, to center them in a taller pane. Uses the list header, which we don't need
    // otherwise, so clicks and scrolling stay lined up on their own
    void setTopPadding(int padding) {
        this.setRenderHeader(padding > 0, Math.max(0, padding));
    }

    // off when the screen draws its own full-width separator instead (the split layout)
    void setDrawFooterSeparator(boolean draw) {
        this.drawFooterSeparator = draw;
    }

    @Override
    protected void renderListSeparators(GuiGraphics graphics) {
        // the top separator is owned by the screen's header bar, so only draw the footer one
        if (!this.drawFooterSeparator) return;
        ResourceLocation footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(footer, this.getX(), this.getBottom(), 0f, 0f, this.getWidth(), 2, 32, 2);
        RenderSystem.disableBlend();
    }
}
