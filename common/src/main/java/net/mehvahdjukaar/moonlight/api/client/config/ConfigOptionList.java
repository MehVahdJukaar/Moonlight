package net.mehvahdjukaar.moonlight.api.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.ROW_WIDTH;

/**
 * The scrolling list of config rows. Uniform row height (a vanilla {@link ContainerObjectSelectionList}
 * constraint); expanded descriptions are represented as extra {@link DescriptionRow}s rather than taller rows.
 */
class ConfigOptionList extends ContainerObjectSelectionList<ConfigListRow> {

    ConfigOptionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
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
        return ROW_WIDTH;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + ROW_WIDTH / 2 + 6;
    }

    @Override
    protected void renderListSeparators(GuiGraphics graphics) {
        // the top separator is owned by the screen's header bar (drawn in renderBackground); only draw the footer one
        ResourceLocation footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        graphics.blit(footer, this.getX(), this.getBottom(), 0f, 0f, this.getWidth(), 2, 32, 2);
        RenderSystem.disableBlend();
    }
}
