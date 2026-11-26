package com.jobportal.service;

import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DiscoveryScheduler {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryScheduler.class);
    private final DiscoveryService discovery;
    public DiscoveryScheduler(DiscoveryService discovery) { this.discovery = discovery; }

    @Scheduled(cron = "${app.scheduler.cron}", zone = "${app.scheduler.zone}")
    public void tick() {
        try { discovery.runAll("SCHEDULED"); }
        catch (IllegalStateException e) { log.warn("Skipped scheduled run: {}", e.getMessage()); }
    }
}
