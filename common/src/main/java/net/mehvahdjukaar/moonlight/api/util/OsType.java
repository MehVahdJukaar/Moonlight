package net.mehvahdjukaar.moonlight.api.util;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Simple operating system family detection based on the {@code os.name} system property.
 */
public enum OsType {
    WINDOWS("windows"),
    MAC("macos"),
    LINUX("linux");

    private static final OsType CURRENT = detect();

    // A launcher started from Finder or the Dock gets a bare PATH, so a Homebrew install is invisible there.
    private static final List<String> EXTRA_MAC_BIN_DIRS = List.of("/opt/homebrew/bin", "/usr/local/bin");

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

    /**
     * Looks for an executable on the user's PATH (plus the usual Homebrew dirs on mac).
     * Takes the base name without extension, e.g. "ffmpeg". Returns null if not found.
     */
    @Nullable
    public Path findExecutable(String baseName) {
        String fileName = executableName(baseName);
        for (String dir : executableSearchDirs()) {
            if (dir.isEmpty()) continue;
            try {
                Path candidate = Paths.get(dir).resolve(fileName);
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                    return candidate.toAbsolutePath();
                }
            } catch (Exception ignored) {
                // malformed PATH entry, skip
            }
        }
        return null;
    }

    private List<String> executableSearchDirs() {
        List<String> dirs = new ArrayList<>();
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null && !pathEnv.isEmpty()) {
            Collections.addAll(dirs, pathEnv.split(File.pathSeparator));
        }
        if (isMac()) {
            dirs.addAll(EXTRA_MAC_BIN_DIRS);
        }
        return dirs;
    }

    private static OsType detect() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) return WINDOWS;
        if (os.contains("mac") || os.contains("darwin")) return MAC;
        return LINUX;
    }
}
