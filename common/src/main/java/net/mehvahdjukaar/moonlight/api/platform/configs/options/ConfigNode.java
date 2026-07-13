package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
