package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * One row of a config screen. Both loaders turn their own config format into a tree of these, so the screen doesn't
 * have to care which one it's on. A node is either a ConfigCategory, which opens a sub screen, or a ConfigOption,
 * which is an editable value.
 */
public abstract class ConfigNode {

    private final Component title;
    @Nullable
    private Component description;
    @Nullable
    private ConfigCategory parent;
    @Nullable
    private ResourceLocation icon;

    protected ConfigNode(Component title, @Nullable Component description) {
        this.title = title;
        this.description = description;
    }

    @ApiStatus.Internal
    public void setParent(ConfigCategory parent) {
        this.parent = parent;
    }

    @Nullable
    public ConfigCategory parent() {
        return parent;
    }

    @ApiStatus.Internal
    public void setDescription(@Nullable Component description) {
        this.description = description;
    }

    @ApiStatus.Internal
    public void setIcon(@Nullable ResourceLocation icon) {
        this.icon = icon;
    }

    @Nullable
    public ResourceLocation icon() {
        return icon;
    }

    public Component title() {
        return title;
    }

    @Nullable
    public Component description() {
        return description;
    }
}
