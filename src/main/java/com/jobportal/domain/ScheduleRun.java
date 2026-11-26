package com.jobportal.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ScheduleRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String triggerType;
    public Instant startedAt;
    public Instant finishedAt;
    public String status;
    public int companiesScanned, jobsFound, newJobs, matchedJobs, duplicates, failures;
    @Column(columnDefinition = "TEXT") public String log;
}
