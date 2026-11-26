package com.jobportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Profile {
    @Id public Long id = 1L;
    public String fullName, email, phone;
    public int experienceYears = 5;
    public String skills, locations, roles, expectedCtc;
    public int noticePeriodDays = 30;
    public int minMatchScore = 60;
    public String resumeUrl, resumeFileName;
    public String defaultPortalUsername, defaultPortalEmail;
    @JsonIgnore public String defaultPortalPasswordEnc;
}
