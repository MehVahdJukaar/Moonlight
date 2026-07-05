package net.mehvahdjukaar.moonlight.core.client.config;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for every row shown in the config list (category button, value row, description line).
 */
abstract class ConfigListRow extends ContainerObjectSelectionList.Entry<ConfigListRow> {

    /**
     * Tooltip to show for the given mouse position, or null for none. Rows decide their own hover region.
     */
    @Nullable
    abstract Component getTooltip(int mouseX, int mouseY);

    /**
     * Tooltip for decorations that sit outside the row's normal hover band (e.g. the reload-hint icon in the
     * left gutter), which {@code getEntryAtPosition} would miss. Queried across all visible rows. Default none.
     */
    @Nullable
    Component getGutterTooltip(int mouseX, int mouseY) {
        return null;
    }
}
