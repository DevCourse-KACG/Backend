package com.back.domain.schedule.schedule.repository;

import com.back.domain.schedule.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 특정 모임의 일정 목록을 시작 날짜 기준으로 오름차순 정렬하여 조회
    List<Schedule> findByClubIdOrderByStartDate(Long clubId);

    // 특정 모임의 활성화된 일정 목록을 시작 날짜 기준으로 오름차순 정렬하여 조회 (비활성화 일정 제외)
    List<Schedule> findByClubIdAndIsActiveTrueOrderByStartDate(Long clubId);

    // 특정 모임의 최신 일정을 ID 기준으로 내림차순 정렬하여 조회
    Optional<Schedule> findFirstByClubIdOrderByIdDesc(Long clubId);

    // 특정 모임의 일정 개수 조회
    int countByClubId(Long clubId);
}
