package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.domain.*;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Workday public career site connector.
 *
 * Workday career pages at *.myworkdayjobs.com expose an internal JSON search
 * endpoint that does not require authentication:
 *
 *   POST https://{tenant}.wd{dc}.myworkdayjobs.com/wday/cxs/{tenant}/{site}/jobs
 *
 * The company's boardToken must be set to the full base URL, e.g.:
 *   https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite
 *
 * The connector extracts tenant, datacenter, and site from that URL to build
 * the search endpoint, then paginates through all results.
 */
@Component
public class WorkdayConnector implements JobConnector {
    private static final Logger log = LoggerFactory.getLogger(WorkdayConnector.class);
    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 15;        // top 300 most recent jobs per company
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://([a-zA-Z0-9_-]+)\\.(wd\\d+)\\.myworkdayjobs\\.com/(.+?)/?$");

    private final RestClient rest = RestClient.builder()
            .defaultHeader("User-Agent", "JobPortalBot/0.1 (personal job search)")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Platform platform() { return Platform.WORKDAY; }

    public record WorkdaySite(String host, String tenant, String dc, String site) {}

    public static WorkdaySite parseSite(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("careersUrl or boardToken must be set to the Workday careers URL, e.g. https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite");
        }
        Matcher m = URL_PATTERN.matcher(baseUrl.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Workday URL must match https://{tenant}.wd{n}.myworkdayjobs.com/{site} — got: " + baseUrl);
        }
        String tenant = m.group(1);
        String dc     = m.group(2);
        String[] parts = m.group(3).split("/");
        // Skip optional locale segment like "en-US" if present
        String site = (parts.length > 1 && parts[0].matches("[a-z]{2}(-[A-Z]{2})?")) ? parts[1] : parts[0];
        if (site.isBlank()) {
            throw new IllegalArgumentException("Workday URL is missing site segment: " + baseUrl);
        }
        String host = tenant + "." + dc + ".myworkdayjobs.com";
        return new WorkdaySite(host, tenant, dc, site);
    }

    @Override
    public List<DiscoveredJob> fetch(Company c) throws Exception {
        String baseUrl = (c.careersUrl != null && !c.careersUrl.isBlank())
                ? c.careersUrl.trim()
                : (c.boardToken != null ? c.boardToken.trim() : "");
        WorkdaySite ws = parseSite(baseUrl);

        String searchUrl = String.format("https://%s/wday/cxs/%s/%s/jobs", ws.host(), ws.tenant(), ws.site());
        String siteBase  = String.format("https://%s/en-US/%s", ws.host(), ws.site());

        List<DiscoveredJob> all = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode result = search(searchUrl, page * PAGE_SIZE, page == 0);
            if (result == null) break;

            int total = result.path("total").asInt(0);
            JsonNode postings = result.path("jobPostings");
            if (postings == null || !postings.isArray() || postings.isEmpty()) break;

            for (JsonNode j : postings) {
                String title        = j.path("title").asText("");
                String extPath      = j.path("externalPath").asText("");
                String locText      = j.path("locationsText").asText("");
                String postedOnText = j.path("postedOn").asText("");
                String jobUrl       = siteBase + extPath;
                String externalId   = extPath.isBlank() ? (title + "|" + locText) : extPath;

                // Workday returns relative dates like "Posted 2 Days Ago", "Posted Today", "Posted 30+ Days Ago"
                Instant postedAt = parsePostedOn(postedOnText);

                all.add(new DiscoveredJob(externalId, title, locText, jobUrl, title, postedAt));
            }

            if (all.size() >= total) break;
        }

        log.info("[WORKDAY] company={} | fetched={} | endpoint={}", c.name, all.size(), searchUrl);
        return all;
    }

    private JsonNode search(String url, int offset, boolean throwOnError) throws Exception {
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "appliedFacets", Map.of(),
                    "limit", PAGE_SIZE,
                    "offset", offset,
                    "searchText", ""
            ));
            String response = rest.post()
                    .uri(URI.create(url))
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return mapper.readTree(response);
        } catch (Exception e) {
            if (throwOnError) throw e;
            log.warn("[WORKDAY WARN] endpoint={} | offset={} | error={}", url, offset, e.getMessage());
            return null;
        }
    }

    /** Parses Workday's relative date strings like "Posted 2 Days Ago", "Posted Today", "Posted 30+ Days Ago". */
    static Instant parsePostedOn(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase().trim();
        if (lower.contains("today") || lower.contains("yesterday")) {
            return lower.contains("yesterday")
                    ? Instant.now().minus(1, ChronoUnit.DAYS)
                    : Instant.now();
        }
        Matcher m = Pattern.compile("(\\d+)\\+?\\s*days?").matcher(lower);
        if (m.find()) {
            int days = Integer.parseInt(m.group(1));
            return Instant.now().minus(days, ChronoUnit.DAYS);
        }
        return null;
    }
}
