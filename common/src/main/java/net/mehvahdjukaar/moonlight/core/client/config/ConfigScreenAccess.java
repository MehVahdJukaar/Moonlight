package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

interface ConfigScreenAccess {

    Font font();

    ConfigEditSession session();

    void openCategory(ConfigCategory category);

    void toggleExpanded(ConfigOption<?> value);

    void onValueEdited();

    boolean isCategoryEnabled(ConfigCategory category);

    default boolean areAncestorsEnabled(ConfigCategory category) {
        ConfigCategory parent = category.parent();
        return parent == null || isCategoryEnabled(parent);
    }

    @Nullable
    default Component featureBlockedBy(ConfigOption.BooleanValue feature) {
        ConfigOption.BooleanValue unmet = unmetDependency(feature);
        if (unmet != null) return unmet.title();
        ConfigCategory owner = feature.parent();
        ConfigCategory from = owner != null && owner.gate() == feature ? owner.parent() : owner;
        for (ConfigCategory c = from; c != null; c = c.parent()) {
            ConfigOption.BooleanValue gate = c.gate();
            if (gate != null && !Boolean.TRUE.equals(session().current(gate))) return c.title();
        }
        return null;
    }

    @Nullable
    default ConfigOption.BooleanValue unmetDependency(ConfigOption.BooleanValue feature) {
        for (ConfigOption.BooleanValue dependency : feature.dependencies()) {
            if (!isFeatureOn(dependency)) return dependency;
        }
        return null;
    }

    private boolean isFeatureOn(ConfigOption.BooleanValue feature) {
        if (!Boolean.TRUE.equals(session().current(feature))) return false;
        ConfigCategory parent = feature.parent();
        return parent == null || isCategoryEnabled(parent);
    }
}
