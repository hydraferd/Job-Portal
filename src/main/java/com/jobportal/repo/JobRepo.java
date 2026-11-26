package com.jobportal.repo;

import com.jobportal.domain.Job;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface JobRepo extends JpaRepository<Job, Long> {
    boolean existsByCompanyIdAndExternalId(Long companyId, String externalId);

    @Query("select j.externalId from Job j where j.company.id = :companyId")
    Set<String> findExternalIdsByCompanyId(@Param("companyId") Long companyId);

    /** Jobs sorted: most recent first, using postedAt when available, else discoveredAt. */
    @Query("select j from Job j order by coalesce(j.postedAt, j.discoveredAt) desc")
    Page<Job> findAllSortedByRecency(Pageable p);

    @Query("select j from Job j where j.matched = :matched order by coalesce(j.postedAt, j.discoveredAt) desc")
    Page<Job> findByMatchedSortedByRecency(@Param("matched") boolean matched, Pageable p);

    /** Filter by applied status, most recent first. */
    @Query("select j from Job j where j.applied = :applied order by coalesce(j.postedAt, j.discoveredAt) desc")
    Page<Job> findByAppliedSortedByRecency(@Param("applied") boolean applied, Pageable p);

    /** Filter by applied AND matched status, most recent first. */
    @Query("select j from Job j where j.applied = :applied and j.matched = :matched order by coalesce(j.postedAt, j.discoveredAt) desc")
    Page<Job> findByAppliedAndMatchedSortedByRecency(@Param("applied") boolean applied, @Param("matched") boolean matched, Pageable p);

    long countByMatchedTrue();

    @Query("select j.company.name, count(j), sum(case when j.matched = true then 1 else 0 end) from Job j group by j.company.name order by j.company.name")
    List<Object[]> statsByCompany();

    // Delete jobs whose posting/discovery date is before the given cutoff. Returns count of deleted rows.
    // @Modifying
    // @Query("delete from Job j where coalesce(j.postedAt, j.discoveredAt) < :cutoff")
    // int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
