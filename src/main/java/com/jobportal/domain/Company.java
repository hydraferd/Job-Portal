package com.jobportal.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Enumerated(EnumType.STRING) public Platform platform;
    /** Board slug for Greenhouse/Lever/Ashby, company identifier for SmartRecruiters. */
    public String boardToken;
    /** Careers page URL, required for CUSTOM. */
    public String careersUrl;
    public boolean enabled = true;
    public Instant lastSyncAt;
}
