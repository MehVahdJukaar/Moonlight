package net.mehvahdjukaar.moonlight.core.client.config;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigRow extends ContainerObjectSelectionList.Entry<ConfigRow> {

    @Nullable
    public abstract Component getTooltip(int mouseX, int mouseY);

    @Nullable
    public Component getGutterTooltip(int mouseX, int mouseY) {
        return null;
    }
}
