package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.ROW_WIDTH;

public class ConfigRowList extends ContainerObjectSelectionList<ConfigRow> {

    private boolean drawFooterSeparator = true;
    private int rowWidth = ROW_WIDTH;

    public ConfigRowList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public void setRows(List<ConfigRow> rows) {
        this.clearEntries();
        for (ConfigRow row : rows) this.addEntry(row);
        this.clampScrollAmount();
    }

    @Nullable
    ConfigRow getHovered(double mouseX, double mouseY) {
        //vanilla doesnt check bottom edge, so without this footer buttons would hover rows
        if (!this.isMouseOver(mouseX, mouseY)) {
            return null;
        }
        return this.getEntryAtPosition(mouseX, mouseY);
    }

    //TODO: remove this stuff in 26.1.2 when row heigh tis ntively supporrted

    private int rowHeight(int index) {
        return this.children().get(index).height(this.itemHeight);
    }

    @Override
    protected int getRowTop(int index) {
        int top = this.getY() + 4 - (int) this.getScrollAmount() + this.headerHeight;
        for (int i = 0; i < index; i++) top += rowHeight(i);
        return top;
    }

    @Override
    protected int getRowBottom(int index) {
        return this.getRowTop(index) + rowHeight(index);
    }

    @Override
    protected int getMaxPosition() {
        int height = this.headerHeight;
        for (int i = 0; i < this.getItemCount(); i++) height += rowHeight(i);
        return height;
    }

    @Nullable
    @Override
    protected ConfigRow getEntryAtPosition(double mouseX, double mouseY) {
        int center = this.getX() + width / 2;
        int halfRow = this.getRowWidth() / 2;
        if (mouseX < center - halfRow || mouseX > center + halfRow) return null;

        int y = Mth.floor(mouseY - this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
        if (y < 0) return null;
        for (int i = 0; i < this.getItemCount(); i++) {
            y -= rowHeight(i);
            if (y < 0) return this.children().get(i);
        }
        return null;
    }

    @Override
    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getRowLeft();
        int width = this.getRowWidth();
        int top = this.getRowTop(0);

        for (int i = 0; i < this.getItemCount(); i++) {
            int height = rowHeight(i);
            if (top + height >= this.getY() && top <= this.getBottom()) {
                this.renderItem(graphics, mouseX, mouseY, partialTick, i, left, top, width, height - 4);
            }
            top += height;
        }
    }

    @Override
    public int getRowWidth() {
        return this.rowWidth;
    }

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
