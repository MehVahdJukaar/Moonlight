package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;

interface ConfigScreenAccess {

    Font font();

    ConfigEditSession session();

    void openCategory(ConfigCategory category);

    void toggleExpanded(ConfigOption<?> value);

    /**
     * Called when a value's working copy changed, so the screen can refresh the Save counter.
     */
    void onValueEdited();

    /**
     * Whether {@code category} is effectively enabled given the current (unsaved) edits: its own feature toggle, if
     * any, and every ancestor's. Rows use this to grey out content whose owning category is switched off.
     */
    boolean isCategoryEnabled(ConfigCategory category);

    /** Whether every ancestor of {@code category} is enabled (ignoring the category's own feature toggle). */
    default boolean areAncestorsEnabled(ConfigCategory category) {
        ConfigCategory parent = category.parent();
        return parent == null || isCategoryEnabled(parent);
    }
}
