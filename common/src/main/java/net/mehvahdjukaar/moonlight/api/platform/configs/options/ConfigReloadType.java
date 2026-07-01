package net.mehvahdjukaar.moonlight.api.platform.configs.options;

/**
 * Whether changing a config value takes effect immediately or needs something reloaded first. Mirrors Forge's
 * {@code worldRestart}/{@code gameRestart} flags (which we still forward on NeoForge), but is kept as our own
 * loader independent enum so the native config screen can also show a matching icon next to the value.
 */
public enum ConfigReloadType {
    /** Applies right away. */
    NONE,
    /** Needs the world to be reloaded/rejoined (Forge {@code worldRestart}). */
    WORLD_RELOAD,
    /** Needs the game to be fully restarted (Forge {@code gameRestart}). */
    GAME_RESTART
}
