package com.jobportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "company_id") public Company company;
    public String externalId;
    public String title;
    public String location;
    public String url;
    @JsonIgnore @Column(columnDefinition = "TEXT") public String description;
    public Instant postedAt;
    public Instant discoveredAt;
    public int matchScore;
    public boolean matched;
    public boolean applied;
    public Instant appliedAt;
}
