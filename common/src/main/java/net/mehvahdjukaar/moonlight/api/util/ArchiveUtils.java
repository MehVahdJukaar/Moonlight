package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ArchiveUtils {
    private static final long MIN_TAR_ARCHIVE_SIZE_BYTES = 1024L;

    // Leading bytes every file of that format starts with. See the "magic number" table in file(1).
    private static final byte[] ZIP_MAGIC = {'P', 'K', 3, 4};
    private static final byte[] XZ_MAGIC = {(byte) 0xFD, '7', 'z', 'X', 'Z', 0};
    private static final byte[] GZIP_MAGIC = {0x1F, (byte) 0x8B};
    private static final byte[] BZIP2_MAGIC = {'B', 'Z', 'h'};
    private static final int MAX_MAGIC_LENGTH = 6;

    public static boolean isSupported(Path archive) {
        String name = archive.getFileName().toString().toLowerCase(Locale.ROOT);
        return isZip(name) || isTarFamily(name);
    }

    /**
     * Guesses the archive extension (".zip", ".tar.xz", ".tar.gz", ".tar.bz2") from the file's leading bytes,
     * for downloads whose URL does not end with a usable file name. Null if the format is not recognized.
     */
    @Nullable
    public static String detectExtension(Path file) throws IOException {
        byte[] head;
        try (InputStream in = Files.newInputStream(file)) {
            head = in.readNBytes(MAX_MAGIC_LENGTH);
        }
        if (startsWith(head, ZIP_MAGIC)) return ".zip";
        if (startsWith(head, XZ_MAGIC)) return ".tar.xz";
        if (startsWith(head, GZIP_MAGIC)) return ".tar.gz";
        if (startsWith(head, BZIP2_MAGIC)) return ".tar.bz2";
        return null;
    }

    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        return Arrays.equals(data, 0, magic.length, magic, 0, magic.length);
    }

    public static boolean isProbablyValid(Path archive) {
        try {
            long size = Files.size(archive);
            if (size == 0) return false;

            String name = archive.getFileName().toString().toLowerCase(Locale.ROOT);
            if (isZip(name)) {
                try (ZipFile ignored = new ZipFile(archive.toFile())) {
                    return true;
                }
            }
            if (isTarFamily(name)) {
                return size > MIN_TAR_ARCHIVE_SIZE_BYTES;
            }
            return false;
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to check if archive is valid: {}", archive, e);
            return false;
        }
    }

    public static void extract(Path archive, Path destination) throws IOException, InterruptedException {
        String name = archive.getFileName().toString().toLowerCase(Locale.ROOT);

        if (isZip(name)) {
            extractZip(archive, destination);
            return;
        }

        if (isTarFamily(name)) {
            extractTar(archive, destination);
            return;
        }

        throw new IOException("Unsupported archive format: " + archive.getFileName());
    }

    private static void extractTar(Path archive, Path destination) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(
                "tar", "-xf", archive.toAbsolutePath().toString(),
                "-C", destination.toAbsolutePath().toString()
        ).start();

        if (p.waitFor() != 0) {
            throw new IOException("Tar extraction failed for " + archive.getFileName());
        }
    }

    private static void extractZip(Path archive, Path destination) throws IOException {
        Path absDestination = destination.toAbsolutePath().normalize();
        try (ZipFile zf = new ZipFile(archive.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();

                Path out = absDestination.resolve(e.getName()).normalize();
                if (!out.startsWith(absDestination)) {
                    throw new IOException("Zip entry escapes destination: " + e.getName());
                }

                if (e.isDirectory()) {
                    Files.createDirectories(out);
                    continue;
                }

                Files.createDirectories(out.getParent());
                try (InputStream is = zf.getInputStream(e)) {
                    Files.copy(is, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static boolean isZip(String fileName) {
        return fileName.endsWith(".zip");
    }

    private static boolean isTarFamily(String fileName) {
        return fileName.endsWith(".tar")
                || fileName.endsWith(".tar.gz")
                || fileName.endsWith(".tgz")
                || fileName.endsWith(".tar.xz")
                || fileName.endsWith(".txz")
                || fileName.endsWith(".tar.bz2")
                || fileName.endsWith(".tbz2");
    }

    public static String extractFileNameFromUrl(String downloadUrl) throws IOException {
        String fileName = Path.of(URI.create(downloadUrl).getPath()).getFileName().toString();
        if (fileName.isEmpty()) {
            throw new IOException("Could not resolve archive name from URL: " + downloadUrl);
        }
        return fileName;
    }

}
