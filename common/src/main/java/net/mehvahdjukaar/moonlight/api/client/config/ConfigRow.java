package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for every row shown in the config list (category button, value row, description line).
 */
abstract class ConfigRow extends ContainerObjectSelectionList.Entry<ConfigRow> {

    /**
     * Tooltip to show for the given mouse position, or null for none. Rows decide their own hover region.
     */
    @Nullable
    abstract Component getTooltip(int mouseX, int mouseY);
}
