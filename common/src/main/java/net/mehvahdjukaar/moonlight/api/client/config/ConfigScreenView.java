package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;

/**
 * The small surface rows use to talk back to their owning screen, so rows can live as their own classes instead
 * of inner classes of a god screen.
 */
interface ConfigScreenView {

    Font font();

    ConfigEditSession session();

    void openCategory(ConfigCategory category);

    void toggleExpanded(ConfigOption<?> value);

    /**
     * Called when a value's working copy changed, so the screen can refresh the Save counter.
     */
    void onValueEdited();
}
