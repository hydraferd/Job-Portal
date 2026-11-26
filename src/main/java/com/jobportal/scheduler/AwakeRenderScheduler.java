package com.jobportal.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AwakeRenderScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(AwakeRenderScheduler.class);

    private final RestClient restClient;

    @Value("${app.render.base-url}")
    private String renderBaseUrl;

    public AwakeRenderScheduler() {
        this.restClient = RestClient.create();
    }

    // Hit Render awake API every 10 minutes
    @Scheduled(fixedDelay = 180000, initialDelay = 10000)
    public void keepRenderAwake() {
        try {
            String response = restClient.get()
                    .uri(renderBaseUrl)
                    .retrieve()
                    .body(String.class);

            log.info("Render awake API response: {}", response);

        } catch (Exception e) {
            log.warn("Render awake API call failed: {}", e.getMessage());
        }
    }
}
