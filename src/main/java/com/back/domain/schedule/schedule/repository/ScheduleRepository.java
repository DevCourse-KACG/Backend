package com.back.domain.schedule.schedule.repository;

import com.back.domain.schedule.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findFirstByClubIdOrderByIdDesc(Long clubId);
    int countByClubId(Long clubId);
}
