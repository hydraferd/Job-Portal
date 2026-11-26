package com.jobportal.service;

import com.jobportal.domain.*;
import java.util.*;
import java.util.regex.*;
import org.springframework.stereotype.Service;

/** Deterministic scoring (0-100): title 20, skills 50, location 15, experience 15. A title miss scores 0. */
@Service
public class MatchService {
    public record MatchResult(int score, boolean matched) {}
    private static final Pattern EXP = Pattern.compile("(\\d{1,2})\\s*\\+?\\s*(?:-\\s*\\d{1,2}\\s*)?(?:years|yrs)");

    public MatchResult evaluate(Job j, Profile p) {
        String title = lower(j.title), desc = lower(j.description), text = title + " " + desc;
        if (csv(p.roles).stream().noneMatch(title::contains)) return new MatchResult(0, false);
        int score = 20;
        List<String> skills = csv(p.skills);
        if (skills.isEmpty()) score += 25;
        else score += (int) Math.round(50.0 * skills.stream().filter(text::contains).count() / skills.size());
        List<String> locs = csv(p.locations);
        String loc = lower(j.location);
        if (locs.isEmpty() || loc.isBlank() || loc.contains("remote") || locs.stream().anyMatch(loc::contains)) score += 15;
        Matcher m = EXP.matcher(desc);
        if (!m.find() || Integer.parseInt(m.group(1)) <= p.experienceYears + 1) score += 15;
        return new MatchResult(score, score >= p.minMatchScore);
    }

    static List<String> csv(String s) {
        if (s == null) return List.of();
        return Arrays.stream(s.split(",")).map(x -> x.trim().toLowerCase()).filter(x -> !x.isEmpty()).toList();
    }
    private static String lower(String s) { return s == null ? "" : s.toLowerCase(); }
}
