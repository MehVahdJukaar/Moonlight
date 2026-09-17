package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.ROW_WIDTH;

public class ConfigRowList extends ContainerObjectSelectionList<ConfigListRow> {

    private boolean drawFooterSeparator = true;
    private int rowWidth = ROW_WIDTH;

    public ConfigRowList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public void setRows(List<ConfigListRow> rows) {
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
        GuiHelper.renderFooterSeparator(graphics, this.getX(), this.getBottom(), this.getWidth());
    }
}
