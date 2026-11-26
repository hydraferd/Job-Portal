package com.jobportal.repo;

import com.jobportal.domain.Company;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepo extends JpaRepository<Company, Long> {
    List<Company> findByEnabledTrue();
}
