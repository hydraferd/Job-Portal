package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobportal.domain.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** Greenhouse Job Board API (public, read-only): https://developers.greenhouse.io/job-board.html */
@Component
public class GreenhouseConnector implements JobConnector {
    private final Http http;
    public GreenhouseConnector(Http http) { this.http = http; }
    public Platform platform() { return Platform.GREENHOUSE; }

    public List<DiscoveredJob> fetch(Company c) {
        return parse(http.getJson("https://boards-api.greenhouse.io/v1/boards/" + c.boardToken + "/jobs?content=true"));
    }

    public List<DiscoveredJob> parse(JsonNode root) {
        List<DiscoveredJob> out = new ArrayList<>();
        for (JsonNode j : root.path("jobs"))
            out.add(new DiscoveredJob(j.path("id").asText(), j.path("title").asText(),
                    j.path("location").path("name").asText(""), j.path("absolute_url").asText(),
                    Text.html(Text.html(j.path("content").asText(""))), Text.iso(j.path("updated_at").asText(null))));
        return out;
    }
}
