package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;

import com.jobportal.domain.*;
import com.jobportal.service.MatchService;
import org.junit.jupiter.api.Test;

class MatchServiceTest {
    private final MatchService svc = new MatchService();

    private Profile profile() {
        Profile p = new Profile();
        p.skills = "java,spring boot,postgresql"; p.roles = "java developer,backend developer";
        p.locations = "bengaluru"; p.experienceYears = 5; p.minMatchScore = 60;
        return p;
    }
    private Job job(String title, String loc, String desc) {
        Job j = new Job(); j.title = title; j.location = loc; j.description = desc; return j;
    }

    @Test void strongMatch() {
        var r = svc.evaluate(job("Senior Java Developer", "Bengaluru, India", "Requires 4+ years of Java, Spring Boot and PostgreSQL"), profile());
        assertEquals(100, r.score());
        assertTrue(r.matched());
    }
    @Test void irrelevantTitleScoresZero() {
        var r = svc.evaluate(job("Marketing Manager", "Bengaluru", "Java Spring Boot PostgreSQL"), profile());
        assertEquals(0, r.score());
        assertFalse(r.matched());
    }
    @Test void weakMatchBelowThreshold() {
        var r = svc.evaluate(job("Java Developer", "Berlin", "Needs 10 years of Java"), profile());
        assertEquals(37, r.score());
        assertFalse(r.matched());
    }
}
