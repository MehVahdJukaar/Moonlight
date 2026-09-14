package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

enum ConfigAvailability {
    EDITABLE(null),
    NOT_IN_WORLD("gui.moonlight.config.unavailable.not_in_world"),
    SERVER_CONTROLLED("gui.moonlight.config.unavailable.server_controlled");

    @Nullable
    private final String reasonKey;

    ConfigAvailability(@Nullable String reasonKey) {
        this.reasonKey = reasonKey;
    }

    boolean isEditable() {
        return this == EDITABLE;
    }

    @Nullable
    Component reason() {
        return reasonKey == null ? null : Component.translatable(reasonKey);
    }

    static ConfigAvailability of(ModConfigHolder holder) {
        if (!holder.isLoaded()) return NOT_IN_WORLD;
        Minecraft mc = Minecraft.getInstance();
        if (holder.isSynced() && mc.getCurrentServer() != null && !mc.isSingleplayer()) return SERVER_CONTROLLED;
        return EDITABLE;
    }
}
