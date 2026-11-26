package com.jobportal.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobportal.config.AppProps;
import java.net.URI;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Http {
    private final RestClient rest = RestClient.builder()
            .defaultHeader("User-Agent", "JobPortalBot/0.1 (personal job search)")
            .defaultHeader("Accept", "application/json").build();
    private final AppProps props;

    public Http(AppProps props) { this.props = props; }

    public JsonNode getJson(String url) {
        int attempts = Math.max(1, props.scheduler().retries());
        RuntimeException last = null;
        for (int i = 1; i <= attempts; i++) {
            try { return rest.get().uri(URI.create(url)).retrieve().body(JsonNode.class); }
            catch (RuntimeException e) {
                last = e;
                try { Thread.sleep(1000L * i); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }
        throw last;
    }
}
