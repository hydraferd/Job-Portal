package com.jobportal.repo;

import com.jobportal.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepo extends JpaRepository<Profile, Long> {}
