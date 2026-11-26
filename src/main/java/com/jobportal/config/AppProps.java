package com.jobportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProps(String encryptionKey, String dataDir, Scheduler scheduler) {
    public record Scheduler(boolean enabled, String cron, String zone, long delayMs, int retries) {}
}
