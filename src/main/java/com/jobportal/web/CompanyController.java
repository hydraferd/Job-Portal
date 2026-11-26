package com.jobportal.web;

import com.jobportal.domain.*;
import com.jobportal.repo.CompanyRepo;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepo repo;
    public CompanyController(CompanyRepo repo) { this.repo = repo; }

    @GetMapping public List<Company> list() { return repo.findAll(); }

    @PostMapping public Company create(@RequestBody Company c) { c.id = null; return repo.save(validate(c)); }

    @PutMapping("/{id}") public Company update(@PathVariable Long id, @RequestBody Company c) {
        Company e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        e.name = c.name; e.platform = c.platform; e.boardToken = c.boardToken; e.careersUrl = c.careersUrl; e.enabled = c.enabled;
        return repo.save(validate(e));
    }

    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { repo.deleteById(id); }

    private Company validate(Company c) {
        if (c.name == null || c.name.isBlank() || c.platform == null) throw new IllegalArgumentException("name and platform are required");
        if (c.platform == Platform.CUSTOM && (c.careersUrl == null || c.careersUrl.isBlank()))
            throw new IllegalArgumentException("careersUrl is required for CUSTOM");
        if (c.platform == Platform.WORKDAY
                && (c.careersUrl == null || c.careersUrl.isBlank())
                && (c.boardToken == null || c.boardToken.isBlank()))
            throw new IllegalArgumentException("careersUrl or boardToken is required for WORKDAY");
        boolean token = c.platform == Platform.GREENHOUSE || c.platform == Platform.LEVER
                || c.platform == Platform.ASHBY || c.platform == Platform.SMARTRECRUITERS;
        if (token && (c.boardToken == null || c.boardToken.isBlank())) throw new IllegalArgumentException("boardToken is required for " + c.platform);
        return c;
    }
}
