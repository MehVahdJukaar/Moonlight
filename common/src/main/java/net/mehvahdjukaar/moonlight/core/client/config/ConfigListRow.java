package net.mehvahdjukaar.moonlight.core.client.config;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

abstract class ConfigListRow extends ContainerObjectSelectionList.Entry<ConfigListRow> {

    @Nullable
    abstract Component getTooltip(int mouseX, int mouseY);

    @Nullable
    Component getGutterTooltip(int mouseX, int mouseY) {
        return null;
    }
}
