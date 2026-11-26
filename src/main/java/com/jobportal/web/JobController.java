package com.jobportal.web;

import com.jobportal.domain.Job;
import com.jobportal.repo.JobRepo;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobRepo repo;
    public JobController(JobRepo repo) { this.repo = repo; }

    @GetMapping
    public Page<Job> list(@RequestParam(required = false) Boolean matched,
                          @RequestParam(required = false) Boolean applied,
                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        if (applied != null && matched != null) return repo.findByAppliedAndMatchedSortedByRecency(applied, matched, p);
        if (applied != null) return repo.findByAppliedSortedByRecency(applied, p);
        if (matched != null) return repo.findByMatchedSortedByRecency(matched, p);
        return repo.findAllSortedByRecency(p);
    }
    @PatchMapping("/{id}/applied")
    public Job setApplied(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Job j = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Job not found"));
        boolean applied = Boolean.TRUE.equals(body.get("applied"));
        j.applied = applied;
        j.appliedAt = applied ? Instant.now() : null;
        return repo.save(j);
    }
}
