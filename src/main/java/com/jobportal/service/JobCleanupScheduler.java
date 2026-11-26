package com.jobportal.service;

import com.jobportal.repo.JobRepo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs every Sunday at 00:00 IST (18:30 UTC Saturday) and deletes
 * jobs that were posted/discovered more than 10 days ago.
 *
 * Enabled by default; disable with app.cleanup.enabled=false.
 */
/*
 * Job cleanup scheduler is disabled (commented out).
 *
@Component
@ConditionalOnProperty(name = "app.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class JobCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobCleanupScheduler.class);
    private final JobRepo jobs;

    public JobCleanupScheduler(JobRepo jobs) { this.jobs = jobs; }

    // Every Sunday at 00:00 IST  (cron: sec min hour dom month dow)
    @Scheduled(cron = "${app.cleanup.cron:0 0 0 * * SUN}", zone = "${app.cleanup.zone:Asia/Kolkata}")
    @Transactional
    public void cleanup() {
        Instant cutoff = Instant.now().minus(10, ChronoUnit.DAYS);
        int deleted = jobs.deleteOlderThan(cutoff);
        log.info("Cleanup: deleted {} jobs older than 10 days (cutoff={})", deleted, cutoff);
    }
}
*/
public class JobCleanupScheduler {
}
