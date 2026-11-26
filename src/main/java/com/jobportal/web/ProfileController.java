package com.jobportal.web;

import com.jobportal.domain.Profile;
import com.jobportal.repo.ProfileRepo;
import com.jobportal.service.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    public record ProfileDto(String fullName, String email, String phone, int experienceYears, String skills,
                             String locations, String roles, String expectedCtc, int noticePeriodDays, int minMatchScore,
                             String resumeUrl, String resumeFileName, String defaultPortalUsername,
                             String defaultPortalEmail, String defaultPortalPassword, boolean defaultPortalPasswordSet) {}

    private final ProfileRepo repo; private final CryptoService crypto; private final ResumeService resumes;
    public ProfileController(ProfileRepo repo, CryptoService crypto, ResumeService resumes) {
        this.repo = repo; this.crypto = crypto; this.resumes = resumes;
    }

    @GetMapping public ProfileDto get() { return toDto(load()); }

    @PutMapping public ProfileDto update(@RequestBody ProfileDto d) {
        if (d.minMatchScore() < 0 || d.minMatchScore() > 100 || d.experienceYears() < 0)
            throw new IllegalArgumentException("minMatchScore must be 0-100 and experienceYears >= 0");
        Profile p = load();
        p.fullName = d.fullName(); p.email = d.email(); p.phone = d.phone(); p.experienceYears = d.experienceYears();
        p.skills = d.skills(); p.locations = d.locations(); p.roles = d.roles(); p.expectedCtc = d.expectedCtc();
        p.noticePeriodDays = d.noticePeriodDays(); p.minMatchScore = d.minMatchScore(); p.resumeUrl = d.resumeUrl();
        p.defaultPortalUsername = d.defaultPortalUsername(); p.defaultPortalEmail = d.defaultPortalEmail();
        if (d.defaultPortalPassword() != null && !d.defaultPortalPassword().isBlank())
            p.defaultPortalPasswordEnc = crypto.encrypt(d.defaultPortalPassword());
        return toDto(repo.save(p));
    }

    /** Downloads the resume from the given URL (or the saved resumeUrl): Google Drive "anyone with link" or direct file URL. */
    @PostMapping("/resume/fetch")
    public ProfileDto fetchResume(@RequestBody(required = false) Map<String, String> body) throws IOException, InterruptedException {
        Profile p = load();
        String url = body != null && body.get("url") != null ? body.get("url") : p.resumeUrl;
        p.resumeFileName = resumes.download(url);
        p.resumeUrl = url;
        return toDto(repo.save(p));
    }

    @PostMapping(value = "/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto upload(@RequestParam("file") MultipartFile file) throws IOException {
        Profile p = load();
        p.resumeFileName = resumes.save(file.getOriginalFilename(), file.getBytes());
        return toDto(repo.save(p));
    }

    @GetMapping("/resume/download")
    public ResponseEntity<Resource> download() {
        Profile p = load();
        if (p.resumeFileName == null) throw new IllegalArgumentException("No resume stored");
        Path path = resumes.path(p.resumeFileName);
        if (!Files.exists(path)) throw new IllegalArgumentException("Resume file missing; upload or fetch it again");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.resumeFileName + "\"")
                .body(new FileSystemResource(path));
    }

    private Profile load() { return repo.findById(1L).orElseThrow(); }

    private ProfileDto toDto(Profile p) {
        return new ProfileDto(p.fullName, p.email, p.phone, p.experienceYears, p.skills, p.locations, p.roles, p.expectedCtc,
                p.noticePeriodDays, p.minMatchScore, p.resumeUrl, p.resumeFileName, p.defaultPortalUsername,
                p.defaultPortalEmail, null, p.defaultPortalPasswordEnc != null);
    }
}
