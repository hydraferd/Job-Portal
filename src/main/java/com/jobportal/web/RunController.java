package com.jobportal.web;

import com.jobportal.domain.ScheduleRun;
import com.jobportal.repo.RunRepo;
import com.jobportal.service.DiscoveryService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private final RunRepo repo; private final DiscoveryService discovery;
    public RunController(RunRepo repo, DiscoveryService discovery) { this.repo = repo; this.discovery = discovery; }

    @GetMapping public List<ScheduleRun> history() { return repo.findTop50ByOrderByStartedAtDesc(); }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger() {
        if (discovery.isRunning()) throw new IllegalStateException("A discovery run is already in progress");
        Thread.startVirtualThread(() -> { try { discovery.runAll("MANUAL"); } catch (IllegalStateException ignored) { } });
        return ResponseEntity.accepted().body(Map.of("status", "started"));
    }
}
