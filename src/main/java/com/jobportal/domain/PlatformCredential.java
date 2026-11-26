package com.jobportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PlatformCredential {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Enumerated(EnumType.STRING) public Platform platform;
    public String username;
    @JsonIgnore public String passwordEnc;
    public Instant updatedAt;
}
