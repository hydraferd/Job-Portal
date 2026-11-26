package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobportal.domain.*;
import java.util.*;
import org.springframework.stereotype.Component;

/** Lever Postings API (public, read-only): https://github.com/lever/postings-api */
@Component
public class LeverConnector implements JobConnector {
    private final Http http;
    public LeverConnector(Http http) { this.http = http; }
    public Platform platform() { return Platform.LEVER; }

    public List<DiscoveredJob> fetch(Company c) {
        return parse(http.getJson("https://api.lever.co/v0/postings/" + c.boardToken + "?mode=json"));
    }

    List<DiscoveredJob> parse(JsonNode root) {
        List<DiscoveredJob> out = new ArrayList<>();
        for (JsonNode j : root)
            out.add(new DiscoveredJob(j.path("id").asText(), j.path("text").asText(),
                    j.path("categories").path("location").asText(""), j.path("hostedUrl").asText(),
                    j.path("descriptionPlain").asText(""), Text.millis(j.path("createdAt").asLong(0))));
        return out;
    }
}
