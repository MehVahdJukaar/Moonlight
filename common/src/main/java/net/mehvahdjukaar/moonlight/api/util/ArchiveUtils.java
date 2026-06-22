package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ArchiveUtils {
    private static final long MIN_TAR_ARCHIVE_SIZE_BYTES = 1024L;

    public static boolean isSupported(Path archive) {
        String name = archive.getFileName().toString().toLowerCase(Locale.ROOT);
        return isZip(name) || isTarFamily(name);
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
