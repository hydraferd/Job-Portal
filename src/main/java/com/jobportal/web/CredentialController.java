package com.jobportal.web;

import com.jobportal.domain.*;
import com.jobportal.repo.CredentialRepo;
import com.jobportal.service.CryptoService;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {
    public record CredentialIn(String username, String password) {}
    public record CredentialOut(Platform platform, String username, String password, Instant updatedAt) {}

    private final CredentialRepo repo; private final CryptoService crypto;
    public CredentialController(CredentialRepo repo, CryptoService crypto) { this.repo = repo; this.crypto = crypto; }

    @GetMapping public List<CredentialOut> list() {
        return repo.findAll().stream().map(c -> new CredentialOut(c.platform, c.username, "********", c.updatedAt)).toList();
    }

    @PutMapping("/{platform}")
    public CredentialOut save(@PathVariable Platform platform, @RequestBody CredentialIn in) {
        if (in.username() == null || in.username().isBlank()) throw new IllegalArgumentException("username is required");
        PlatformCredential c = repo.findByPlatform(platform).orElseGet(PlatformCredential::new);
        if ((in.password() == null || in.password().isBlank()) && c.passwordEnc == null) throw new IllegalArgumentException("password is required");
        c.platform = platform; c.username = in.username(); c.updatedAt = Instant.now();
        if (in.password() != null && !in.password().isBlank()) c.passwordEnc = crypto.encrypt(in.password());
        repo.save(c);
        return new CredentialOut(platform, c.username, "********", c.updatedAt);
    }

    @DeleteMapping("/{platform}")
    public void delete(@PathVariable Platform platform) { repo.findByPlatform(platform).ifPresent(repo::delete); }
}
