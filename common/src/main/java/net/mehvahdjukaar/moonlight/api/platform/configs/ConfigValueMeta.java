package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Change-effect metadata of a single leaf config value — "what happens when this value changes". The leaf value is the
 * source of truth for both facets:
 * <ul>
 *     <li>{@link #reloadType()} — whether a change needs a world reload / game restart (read by the screen to show an
 *     icon), and</li>
 *     <li>{@link #affectsDynamicPacks()} — whether a change should invalidate dynamic resource/data packs (read by the
 *     {@link ModConfigHolder} on disk edit, sync, or screen edit).</li>
 * </ul>
 * Both live here, together, because they are the same kind of fact measured on the same object. A grouped row (a range
 * or vec3) is <em>not</em> a source of truth: it is just a presentation grouping of several leaves, and its
 * {@code ConfigOption} derives its shown metadata by aggregating its members (highest reload severity wins). This is
 * kept off {@link ConfigValueHandle} on purpose — the value handle a mod receives has no business touching it. The
 * setters are only ever called by the builder while assembling the config.
 */
@ApiStatus.Internal
public interface ConfigValueMeta {

    /** Whether changing this value needs a world reload / game restart before it takes effect. */
    ConfigReloadType reloadType();

    /** Build-time only: called by the builder to set this value's reload effect. */
    void setReloadType(ConfigReloadType reloadType);

    /** Whether changing this value should invalidate dynamic resource/data packs. */
    boolean affectsDynamicPacks();

    /** Build-time only: called by the builder to mark this value as pack-affecting. */
    void setAffectsDynamicPacks(boolean affectsDynamicPacks);
}
