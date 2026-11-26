package com.jobportal.repo;

import com.jobportal.domain.ScheduleRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RunRepo extends JpaRepository<ScheduleRun, Long> {
    List<ScheduleRun> findTop50ByOrderByStartedAtDesc();

    @Modifying
    @Query("update ScheduleRun r set r.status = 'INTERRUPTED', r.finishedAt = CURRENT_TIMESTAMP where r.status = 'RUNNING'")
    int markStaleRunsInterrupted();
}
