package com.jobportal.repo;

import com.jobportal.domain.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepo extends JpaRepository<PlatformCredential, Long> {
    Optional<PlatformCredential> findByPlatform(Platform platform);
}
