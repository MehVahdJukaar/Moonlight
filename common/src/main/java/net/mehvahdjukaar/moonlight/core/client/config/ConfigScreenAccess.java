package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface ConfigScreenAccess {

    Font font();

    ConfigEditSession session();

    void openCategory(ConfigCategory category);

    void toggleExpanded(ConfigOption<?> value);

    default boolean hasDescriptionPanel() {
        return false;
    }

    void onValueEdited();

    boolean isCategoryEnabled(ConfigCategory category);

    default boolean areAncestorsEnabled(ConfigCategory category) {
        ConfigCategory parent = category.parent();
        return parent == null || isCategoryEnabled(parent);
    }

    @Nullable
    default Component featureBlockedBy(ConfigOption.BooleanOption feature) {
        ConfigOption.BooleanOption unmet = unmetDependency(feature);
        if (unmet != null) return unmet.title();
        ConfigCategory owner = feature.parent();
        ConfigCategory from = owner != null && owner.gate() == feature ? owner.parent() : owner;
        for (ConfigCategory c = from; c != null; c = c.parent()) {
            ConfigOption.BooleanOption gate = c.gate();
            if (gate != null && !Boolean.TRUE.equals(session().valueOrPendingValue(gate))) return c.title();
        }
        return null;
    }

    @Nullable
    default ConfigOption.BooleanOption unmetDependency(ConfigOption.BooleanOption feature) {
        for (ConfigOption.BooleanOption dependency : feature.dependencies()) {
            if (!isFeatureOn(dependency)) return dependency;
        }
        return null;
    }

    private boolean isFeatureOn(ConfigOption.BooleanOption feature) {
        if (!Boolean.TRUE.equals(session().valueOrPendingValue(feature))) return false;
        ConfigCategory parent = feature.parent();
        return parent == null || isCategoryEnabled(parent);
    }
}
