package net.mehvahdjukaar.moonlight.api.util;

import java.util.Locale;

/**
 * Simple operating system family detection based on the {@code os.name} system property.
 */
public enum OsType {
    WINDOWS("windows"),
    MAC("macos"),
    LINUX("linux");

    private static final OsType CURRENT = detect();

    private final String key;

    OsType(String key) {
        this.key = key;
    }

    public static OsType current() {
        return CURRENT;
    }

    /**
     * Lowercase identifier for this OS ({@code windows}, {@code macos}, {@code linux}).
     * Handy as a key into per-OS config maps or download source tables.
     */
    public String key() {
        return key;
    }

    public boolean isWindows() {
        return this == WINDOWS;
    }

    public boolean isMac() {
        return this == MAC;
    }

    public boolean isLinux() {
        return this == LINUX;
    }

    /**
     * Whether files on this OS need their executable bit set manually (true on Unix-likes, false on Windows).
     */
    public boolean requiresExecutableBit() {
        return this != WINDOWS;
    }

    /**
     * Resolves a native executable file name for this OS, appending {@code .exe} on Windows.
     */
    public String executableName(String baseName) {
        return this == WINDOWS ? baseName + ".exe" : baseName;
    }

    private static OsType detect() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) return WINDOWS;
        if (os.contains("mac") || os.contains("darwin")) return MAC;
        return LINUX;
    }
}
