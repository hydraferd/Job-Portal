package com.jobportal.service;

import com.jobportal.config.AppProps;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.regex.*;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private final Path dir;
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(20)).build();

    public ResumeService(AppProps props) throws IOException {
        dir = Path.of(props.dataDir(), "resume");
        Files.createDirectories(dir);
    }

    /** Converts Google Drive share links to direct-download links; other URLs pass through. */
    static String normalize(String url) {
        Matcher m = Pattern.compile("drive\\.google\\.com/file/d/([\\w-]+)").matcher(url);
        if (m.find()) return "https://drive.google.com/uc?export=download&id=" + m.group(1);
        m = Pattern.compile("drive\\.google\\.com/.*[?&]id=([\\w-]+)").matcher(url);
        if (m.find()) return "https://drive.google.com/uc?export=download&id=" + m.group(1);
        return url;
    }

    public String download(String url) throws IOException, InterruptedException {
        if (url == null || !url.matches("(?i)https?://.+")) throw new IllegalArgumentException("A valid http(s) resume URL is required");
        HttpResponse<byte[]> r = client.send(HttpRequest.newBuilder(URI.create(normalize(url)))
                .timeout(Duration.ofSeconds(60)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        if (r.statusCode() != 200) throw new IllegalArgumentException("Resume URL returned HTTP " + r.statusCode());
        String ct = r.headers().firstValue("content-type").orElse("");
        if (ct.contains("text/html"))
            throw new IllegalArgumentException("URL did not return a file. For Google Drive, share the file as 'Anyone with the link'.");
        String name = r.headers().firstValue("content-disposition")
                .map(h -> { Matcher m = Pattern.compile("filename=\"?([^\";]+)").matcher(h); return m.find() ? m.group(1) : null; })
                .orElse("resume.pdf");
        return save(name, r.body());
    }

    public String save(String originalName, byte[] data) throws IOException {
        if (data.length == 0 || data.length > MAX_BYTES) throw new IllegalArgumentException("Resume must be between 1 byte and 10 MB");
        String name = (originalName == null ? "resume.pdf" : originalName).replaceAll("[^A-Za-z0-9._-]", "_");
        if (!name.toLowerCase().matches(".*\\.(pdf|doc|docx)$")) name += ".pdf";
        Files.write(dir.resolve(name), data);
        return name;
    }

    public Path path(String name) { return dir.resolve(Path.of(name).getFileName().toString()); }
}
