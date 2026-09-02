package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Http downloader that resumes partial files and retries on transient failures.
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
            //a 4xx wont change on a retry, aside from request timeout and rate limit
            if (statusCode >= 400 && statusCode < 500) {
                return statusCode == 408 || statusCode == 429;
            }
            return true;
        }
    }

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
        Path tmp = target.resolveSibling(target.getFileName() + ".part");
        long resumeFrom = Files.exists(tmp) ? Files.size(tmp) : 0;

        Moonlight.LOGGER.info("Downloading {} ...", urlStr);

        int attempt = 0;
        while (true) {
            try {
                long size = downloadAttempt(urlStr, tmp, resumeFrom, userAgent, progressCallback);
                Moonlight.LOGGER.info("Downloaded {} bytes from {}", size, urlStr);
                break;
            } catch (IOException e) {
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
                    Thread.sleep(1000L * attempt); //progressive backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", ie);
                }
                //pick up from whatever made it to disk
                resumeFrom = Files.exists(tmp) ? Files.size(tmp) : 0;
            }
        }

        moveFileAtomically(tmp, target);
    }

    public static byte[] readBytes(String urlStr) throws IOException {
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
     * Fetches a URL fully into memory as a UTF-8 string
     */
    public static String readString(String urlStr) throws IOException {
        return new String(readBytes(urlStr), StandardCharsets.UTF_8);
    }

    public static void moveFileAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (UnsupportedOperationException | IOException e) {
            Moonlight.LOGGER.info("Atomic move not supported, using standard move: {}", e.getMessage());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static HttpURLConnection createConnection(String urlStr, long startOffset,
                                                      @Nullable String userAgent) throws IOException {
        URI uri;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            throw new IOException("Malformed URL: " + urlStr, e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IOException("Unsupported protocol: " + scheme + ". Only HTTP/HTTPS are allowed.");
        }

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
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

    //returns the size the file ended up with
    private static long downloadAttempt(String urlStr, Path tmp, long startOffset,
                                        @Nullable String userAgent,
                                        @Nullable ProgressCallback progressCallback) throws IOException {
        HttpURLConnection conn = createConnection(urlStr, startOffset, userAgent);
        try {
            int responseCode = conn.getResponseCode();

            //asked to resume but got a whole body back, so start over
            if (startOffset > 0 && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                Moonlight.LOGGER.info("Server does not support range requests (code {}). Restarting download from 0.", responseCode);
                conn.disconnect();
                conn = createConnection(urlStr, 0, userAgent);
                responseCode = conn.getResponseCode();
                startOffset = 0;
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new HttpStatusException(responseCode, urlStr);
            }

            //on a 206 the length is just what's left, and startOffset is 0 in every other case
            long contentLength = conn.getContentLengthLong();
            long totalExpected = contentLength < 0 ? -1 : contentLength + startOffset;

            StandardOpenOption[] writeOptions = startOffset > 0
                    ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(tmp, writeOptions)) {

                byte[] buffer = new byte[16384];
                long downloaded = startOffset;
                int lastPercent = -1;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    if (totalExpected > 0) {
                        int percent = (int) (downloaded * 100 / totalExpected);
                        if (percent != lastPercent) {
                            Moonlight.LOGGER.debug("Downloading {} ... {}%", tmp.getFileName(), percent);
                            if (progressCallback != null) {
                                progressCallback.onProgress(percent);
                            }
                            lastPercent = percent;
                        }
                    }
                }
            }

            long actual = Files.size(tmp);
            if (totalExpected > 0 && actual != totalExpected) {
                throw new IOException(String.format(
                        "Incomplete download: expected %d bytes, got %d bytes", totalExpected, actual));
            }
            return actual;
        } finally {
            conn.disconnect();
        }
    }
}
