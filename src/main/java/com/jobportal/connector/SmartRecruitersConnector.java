package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobportal.domain.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** SmartRecruiters public Posting API: https://developers.smartrecruiters.com/docs/posting-api */
@Component
public class SmartRecruitersConnector implements JobConnector {
    private static final int PAGE = 100, MAX_PAGES = 20;
    private final Http http;
    public SmartRecruitersConnector(Http http) { this.http = http; }
    public Platform platform() { return Platform.SMARTRECRUITERS; }

    public List<DiscoveredJob> fetch(Company c) {
        List<DiscoveredJob> all = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode root = http.getJson("https://api.smartrecruiters.com/v1/companies/" + c.boardToken
                    + "/postings?limit=" + PAGE + "&offset=" + page * PAGE);
            List<DiscoveredJob> batch = parse(root, c.boardToken);
            all.addAll(batch);
            if (batch.size() < PAGE) break;
        }
        return all;
    }

    List<DiscoveredJob> parse(JsonNode root, String company) {
        List<DiscoveredJob> out = new ArrayList<>();
        for (JsonNode j : root.path("content")) {
            String loc = j.path("location").path("city").asText("");
            String country = j.path("location").path("country").asText("");
            if (!country.isBlank()) loc = loc.isBlank() ? country : loc + ", " + country;
            String id = j.path("id").asText();
            // The list endpoint carries no description; the title is used for matching.
            out.add(new DiscoveredJob(id, j.path("name").asText(), loc,
                    "https://jobs.smartrecruiters.com/" + company + "/" + id,
                    j.path("name").asText(), Text.iso(j.path("releasedDate").asText(null))));
        }
        return out;
    }
}
