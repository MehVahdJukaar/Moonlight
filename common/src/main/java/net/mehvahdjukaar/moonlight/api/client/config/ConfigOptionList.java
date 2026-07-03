package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.ROW_WIDTH;

/**
 * The scrolling list of config rows. Uniform row height (a vanilla {@link ContainerObjectSelectionList}
 * constraint); expanded descriptions are represented as extra {@link DescriptionRow}s rather than taller rows.
 */
class ConfigOptionList extends ContainerObjectSelectionList<ConfigRow> {

    ConfigOptionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    void setRows(List<ConfigRow> rows) {
        this.clearEntries();
        for (ConfigRow row : rows) this.addEntry(row);
        this.clampScrollAmount();
    }

    @Nullable
    ConfigRow getHovered(double mouseX, double mouseY) {
        return this.getEntryAtPosition(mouseX, mouseY);
    }

    @Override
    public int getRowWidth() {
        return ROW_WIDTH;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + ROW_WIDTH / 2 + 6;
    }
}
