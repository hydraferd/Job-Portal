package com.jobportal.web;

import com.jobportal.connector.ConnectorFactory;
import com.jobportal.domain.Platform;
import com.jobportal.repo.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StatsController {
    public record CompanyStat(String company, long discovered, long matched) {}
    public record PlatformInfo(Platform platform, boolean discovery, boolean autoApply, String note) {}

    private final JobRepo jobs; private final CompanyRepo companies; private final RunRepo runs; private final ConnectorFactory factory;
    public StatsController(JobRepo jobs, CompanyRepo companies, RunRepo runs, ConnectorFactory factory) {
        this.jobs = jobs; this.companies = companies; this.runs = runs; this.factory = factory;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<CompanyStat> byCompany = jobs.statsByCompany().stream()
                .map(r -> new CompanyStat((String) r[0], ((Number) r[1]).longValue(), ((Number) r[2]).longValue())).toList();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("companies", companies.count());
        m.put("discovered", jobs.count());
        m.put("matched", jobs.countByMatchedTrue());
        m.put("lastRun", runs.findTop50ByOrderByStartedAtDesc().stream().findFirst().orElse(null));
        m.put("byCompany", byCompany);
        return m;
    }

    /** Capability matrix: what is supported today. Auto-apply is not implemented in this release. */
    @GetMapping("/platforms")
    public List<PlatformInfo> platforms() {
        return Arrays.stream(Platform.values()).map(p -> new PlatformInfo(p, factory.find(p).isPresent(), false, switch (p) {
            case GREENHOUSE, LEVER, ASHBY, SMARTRECRUITERS -> "Discovery via public read-only job API";
            case CUSTOM -> "Best-effort static HTML link discovery; no JavaScript rendering";
            case WORKDAY -> "Discovery via Workday public career site (*.myworkdayjobs.com) — set boardToken to full careers URL";
            default -> "No public job API; discovery not supported in this release";
        })).toList();
    }
}
