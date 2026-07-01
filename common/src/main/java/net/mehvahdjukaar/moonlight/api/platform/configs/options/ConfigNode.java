package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Loader independent description of a single row in a config screen.
 * Both Fabric and NeoForge config holders translate their internal config representation into a tree
 * of these so that the actual screen ({@code MoonlightConfigScreen}) can stay completely platform agnostic.
 * <p>
 * An entry is either a {@link ConfigCategory} (navigable, opens a sub screen) or a
 * {@link ConfigOption} (an editable leaf value).
 */
public abstract class ConfigNode {

    private final Component title;
    @Nullable
    private Component description;
    @Nullable
    private ConfigCategory parent;

    protected ConfigNode(Component title, @Nullable Component description) {
        this.title = title;
        this.description = description;
    }

    /** Set when this node is added to a category, so the screen can walk the category chain (e.g. for feature gating). */
    @ApiStatus.Internal
    public void setParent(ConfigCategory parent) {
        this.parent = parent;
    }

    /** The category this node lives directly under, or null for the root. */
    @Nullable
    public ConfigCategory parent() {
        return parent;
    }

    /**
     * Sets the description after construction. The config builder uses this so a {@code comment(...)} call can
     * attach a description whether it comes before or after the value's {@code define(...)}.
     */
    @ApiStatus.Internal
    public void setDescription(@Nullable Component description) {
        this.description = description;
    }

    /**
     * Display name of this entry (category button label or value label).
     */
    public Component title() {
        return title;
    }

    /**
     * Optional tooltip/comment shown when hovering this row. May be null.
     */
    @Nullable
    public Component description() {
        return description;
    }
}
