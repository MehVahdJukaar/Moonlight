package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * A resilient HTTP downloader with resuming, retries, and progress reporting.
 */
public final class FileDownloadUtils {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static final int MAX_ATTEMPTS = 8;

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int percent);
    }

    @FunctionalInterface
    public interface RetryCallback {
        void onRetry(int failedAttempt, int maxAttempts, IOException cause);
    }

    public static class HttpStatusException extends IOException {
        public final int statusCode;

        public HttpStatusException(int statusCode, String url) {
            super(String.format("HTTP %d for URL: %s", statusCode, url));
            this.statusCode = statusCode;
        }

        public boolean isRetryable() {
            // Client errors are deterministic and won't change on retry -- except request timeout
            // (408) and rate limiting (429), which may succeed after backoff.
            if (statusCode >= 400 && statusCode < 500) {
                return statusCode == 408 || statusCode == 429;
            }
            return true;
        }
    }

    // Public API -------------------------------------------------------------

    public static void download(String urlStr, Path target) throws IOException {
        download(urlStr, target, null, null, null);
    }

    public static void download(String urlStr, Path target, @Nullable String userAgent) throws IOException {
        download(urlStr, target, userAgent, null, null);
    }

    public static void download(String urlStr, Path target,
                                @Nullable String userAgent,
                                @Nullable ProgressCallback progressCallback) throws IOException {
        download(urlStr, target, userAgent, progressCallback, null);
    }

    public static void download(String urlStr, Path target,
                                @Nullable String userAgent,
                                @Nullable ProgressCallback progressCallback,
                                @Nullable RetryCallback retryCallback) throws IOException {

        validateUrl(urlStr);

        Path tmp = target.resolveSibling(target.getFileName() + ".part");
        long downloadedBytes = Files.exists(tmp) ? Files.size(tmp) : 0;

        Moonlight.LOGGER.info("Downloading {} ...", urlStr);

        int attempt = 0;
        while (true) {
            try {
                downloadAttempt(urlStr, tmp, downloadedBytes, userAgent, progressCallback);
                break; // success
            } catch (IOException e) {
                // Deterministic failures (e.g. HTTP 403/404) can never succeed; don't waste retries.
                if (e instanceof HttpStatusException hse && !hse.isRetryable()) {
                    Files.deleteIfExists(tmp);
                    throw e;
                }
                attempt++;
                if (attempt >= MAX_ATTEMPTS) {
                    Files.deleteIfExists(tmp);
                    throw new IOException("Failed to download after " + MAX_ATTEMPTS + " attempts: " + urlStr, e);
                }
                Moonlight.LOGGER.warn("Download attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                if (retryCallback != null) {
                    retryCallback.onRetry(attempt, MAX_ATTEMPTS, e);
                }
                try {
                    Thread.sleep(1000L * attempt); // progressive backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", ie);
                }
                // Re‑read the size of the partial file for the next attempt
                downloadedBytes = Files.exists(tmp) ? Files.size(tmp) : 0;
            }
        }

        Moonlight.LOGGER.info("Downloaded {} bytes from {}", downloadedBytes, urlStr);


        // Move the completed temporary file to the final destination
        moveFileAtomically(tmp, target);
    }

    /**
     * Fetches a URL fully into memory as raw bytes. For large payloads prefer {@link #download} to a file.
     */
    public static byte[] readBytes(String urlStr) throws IOException {
        validateUrl(urlStr);
        HttpURLConnection conn = createConnection(urlStr, 0, null);
        try {
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) throw new HttpStatusException(code, urlStr);
            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Fetches a URL fully into memory as a UTF-8 string.
     */
    public static String readString(String urlStr) throws IOException {
        return new String(readBytes(urlStr), StandardCharsets.UTF_8);
    }

    // Private helpers --------------------------------------------------------

    private static void validateUrl(String urlStr) throws IOException {
        try {
            URI uri = new URI(urlStr);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IOException("Unsupported protocol: " + scheme + ". Only HTTP/HTTPS are allowed.");
            }
        } catch (URISyntaxException e) {
            throw new IOException("Malformed URL: " + urlStr, e);
        }
    }

    private static void moveFileAtomically(Path source, Path target) throws IOException {
        try {
            // Try atomic move first (works on most local file systems)
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (UnsupportedOperationException | IOException e) {
            // Fallback: non‑atomic move (copy + delete) – still safer than leaving a .part file
            Moonlight.LOGGER.info("Atomic move not supported, using standard move: {}", e.getMessage());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static HttpURLConnection createConnection(String urlStr, long startOffset,
                                                      @Nullable String userAgent) throws IOException {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        if (userAgent != null) {
            conn.setRequestProperty("User-Agent", userAgent);
        }

        if (startOffset > 0) {
            conn.setRequestProperty("Range", "bytes=" + startOffset + "-");
        }
        return conn;
    }

    private static void downloadAttempt(String urlStr, Path tmp, long startOffset,
                                        @Nullable String userAgent,
                                        @Nullable ProgressCallback progressCallback) throws IOException {
        HttpURLConnection conn = createConnection(urlStr, startOffset, userAgent);
        int responseCode;
        long actualStartOffset = startOffset;
        boolean rangeSupported = true;

        try {
            responseCode = conn.getResponseCode();

            // If we requested a range but the server doesn't support it, restart from zero
            if (startOffset > 0 && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                Moonlight.LOGGER.info("Server does not support range requests (code {}). Restarting download from 0.", responseCode);
                conn.disconnect();
                rangeSupported = false;
                actualStartOffset = 0;
                conn = createConnection(urlStr, 0, userAgent);
                responseCode = conn.getResponseCode();
            }

            if (responseCode < 200 || responseCode >= 300) {
                throw new HttpStatusException(responseCode, urlStr);
            }

            // Determine expected total size
            long contentLength = conn.getContentLengthLong();
            long totalExpected = (rangeSupported && responseCode == HttpURLConnection.HTTP_PARTIAL)
                    ? contentLength + actualStartOffset
                    : contentLength;

            // Prepare output stream – append if resuming, otherwise create/truncate
            boolean append = rangeSupported && actualStartOffset > 0 && responseCode == HttpURLConnection.HTTP_PARTIAL;
            StandardOpenOption[] writeOptions = append
                    ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(tmp, writeOptions)) {

                byte[] buffer = new byte[16384];
                long downloaded = actualStartOffset;
                int lastPercent = -1;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    if (totalExpected > 0) {
                        int percent = (int) (downloaded * 100 / totalExpected);
                        if (percent != lastPercent) {
                            // Use debug level to avoid console spam – change to INFO only if needed
                            Moonlight.LOGGER.info("Downloading {} ... {}%", tmp.getFileName(), percent);
                            if (progressCallback != null) {
                                progressCallback.onProgress(percent);
                            }
                            lastPercent = percent;
                        }
                    }
                }
            }

            // Final size validation (only if we know the expected size)
            if (totalExpected > 0 && Files.size(tmp) != totalExpected) {
                throw new IOException(String.format(
                        "Incomplete download: expected %d bytes, got %d bytes", totalExpected, Files.size(tmp)));
            }

        } finally {
            conn.disconnect();
        }
    }
}
