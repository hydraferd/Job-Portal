package com.jobportal.service;

import com.jobportal.config.AppProps;
import com.jobportal.connector.*;
import com.jobportal.domain.*;
import com.jobportal.repo.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscoveryService {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryService.class);
    private final AtomicBoolean running = new AtomicBoolean();
    private final CompanyRepo companies; private final JobRepo jobs; private final RunRepo runs;
    private final ProfileRepo profiles; private final ConnectorFactory factory;
    private final MatchService match; private final AppProps props;

    public DiscoveryService(CompanyRepo companies, JobRepo jobs, RunRepo runs, ProfileRepo profiles,
                            ConnectorFactory factory, MatchService match, AppProps props) {
        this.companies = companies; this.jobs = jobs; this.runs = runs; this.profiles = profiles;
        this.factory = factory; this.match = match; this.props = props;
    }

    public boolean isRunning() { return running.get(); }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupStaleRuns() {
        int updated = runs.markStaleRunsInterrupted();
        if (updated > 0) {
            log.warn("[STARTUP] Marked {} stale RUNNING discovery run(s) as INTERRUPTED", updated);
        }
    }

    public ScheduleRun runAll(String trigger) {
        if (!running.compareAndSet(false, true)) throw new IllegalStateException("A discovery run is already in progress");
        long runStartMs = System.currentTimeMillis();
        ScheduleRun run = new ScheduleRun();
        run.triggerType = trigger; run.startedAt = Instant.now(); run.status = "RUNNING";
        run = runs.save(run);
        StringBuilder sb = new StringBuilder();
        try {
            Profile profile = profiles.findById(1L).orElseThrow();
            List<Company> enabled = companies.findByEnabledTrue();
            log.info("[DISCOVERY START] trigger={} | enabledCompanies={}", trigger, enabled.size());
            for (Company c : enabled) {
                run.companiesScanned++;
                Optional<JobConnector> connector = factory.find(c.platform);
                if (connector.isEmpty()) {
                    run.failures++;
                    String msg = String.format("[PULL SKIP] company=%s | platform=%s | reason=discovery not supported", c.name, c.platform);
                    log.warn(msg);
                    sb.append(msg).append('\n');
                    continue;
                }
                try {
                    List<DiscoveredJob> found = connector.get().fetch(c);
                    int pulled = found.size();
                    int saved = 0, matched = 0, duplicates = 0, skipped = 0;
                    run.jobsFound += pulled;
                    Set<String> existingIds = new HashSet<>(jobs.findExternalIdsByCompanyId(c.id));
                    List<Job> toSave = new ArrayList<>();
                    for (DiscoveredJob d : found) {
                        Job job = prepareJob(c, d, profile, existingIds, run);
                        if (job == null) {
                            if (d.externalId() != null && existingIds.contains(d.externalId())) duplicates++;
                            else skipped++;
                        } else {
                            toSave.add(job);
                            existingIds.add(job.externalId);
                            saved++;
                            if (job.matched) matched++;
                        }
                    }
                    if (!toSave.isEmpty()) {
                        jobs.saveAll(toSave);
                    }
                    c.lastSyncAt = Instant.now();
                    companies.save(c);
                    String okMsg = String.format("[PULL OK]   company=%s | platform=%s | pulled=%d | saved=%d | matched=%d | duplicates=%d | skipped=%d",
                            c.name, c.platform, pulled, saved, matched, duplicates, skipped);
                    log.info(okMsg);
                    sb.append(okMsg).append('\n');
                } catch (Exception e) {
                    run.failures++;
                    String errMsg = String.format("[PULL FAIL] company=%s | platform=%s | error=%s",
                            c.name, c.platform, e.getMessage());
                    log.error(errMsg);
                    sb.append(errMsg).append('\n');
                }
                pause();
            }
            run.status = run.failures == 0 ? "SUCCESS" : run.failures < run.companiesScanned ? "PARTIAL" : "FAILED";
        } catch (Exception e) {
            run.status = "FAILED";
            String errMsg = String.format("[DISCOVERY ERROR] error=%s", e.getMessage());
            sb.append(errMsg).append('\n');
            log.error(errMsg, e);
        } finally {
            run.finishedAt = Instant.now();
            run.log = sb.length() > 20000 ? sb.substring(0, 20000) : sb.toString();
            run = runs.save(run);
            running.set(false);
            long elapsedMs = System.currentTimeMillis() - runStartMs;
            log.info("[DISCOVERY END]   status={} | companies={} | pulled={} | saved={} | matched={} | duplicates={} | failures={} | durationMs={}",
                    run.status, run.companiesScanned, run.jobsFound, run.newJobs, run.matchedJobs, run.duplicates, run.failures, elapsedMs);
        }
        return run;
    }

    /** Evaluates and prepares a Job for batch save; returns null if irrelevant or duplicate. */
    private Job prepareJob(Company c, DiscoveredJob d, Profile profile, Set<String> existingIds, ScheduleRun run) {
        Job j = new Job();
        j.company = c; j.externalId = d.externalId(); j.title = d.title(); j.location = d.location();
        j.url = d.url(); j.postedAt = d.postedAt(); j.discoveredAt = Instant.now();
        j.description = d.description() != null && d.description().length() > 20000 ? d.description().substring(0, 20000) : d.description();
        MatchService.MatchResult m = match.evaluate(j, profile);
        if (m.score() == 0) return null;                   // role not relevant: not stored
        if (existingIds.contains(j.externalId)) { run.duplicates++; return null; }
        j.matchScore = m.score(); j.matched = m.matched();
        run.newJobs++;
        if (m.matched()) run.matchedJobs++;
        return j;
    }

    private void pause() {
        try { Thread.sleep(props.scheduler().delayMs()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
