package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobportal.domain.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** Ashby public Job Postings API: https://developers.ashbyhq.com/docs/public-job-posting-api */
@Component
public class AshbyConnector implements JobConnector {
    private final Http http;
    public AshbyConnector(Http http) { this.http = http; }
    public Platform platform() { return Platform.ASHBY; }

    public List<DiscoveredJob> fetch(Company c) {
        return parse(http.getJson("https://api.ashbyhq.com/posting-api/job-board/" + c.boardToken));
    }

    List<DiscoveredJob> parse(JsonNode root) {
        List<DiscoveredJob> out = new ArrayList<>();
        for (JsonNode j : root.path("jobs"))
            out.add(new DiscoveredJob(j.path("id").asText(), j.path("title").asText(),
                    j.path("location").asText(""), j.path("jobUrl").asText(),
                    j.path("descriptionPlain").asText(""), Text.iso(j.path("publishedAt").asText(null))));
        return out;
    }
}
