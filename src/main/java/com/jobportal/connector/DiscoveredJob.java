package com.jobportal.connector;

import java.time.Instant;

public record DiscoveredJob(String externalId, String title, String location, String url, String description, Instant postedAt) {}
