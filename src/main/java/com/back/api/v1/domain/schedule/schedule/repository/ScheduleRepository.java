package com.back.api.v1.domain.schedule.schedule.repository;

import com.back.api.v1.domain.schedule.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

}
