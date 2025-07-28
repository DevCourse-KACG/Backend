package com.back.domain.schedule.schedule.dto;

import com.back.domain.schedule.schedule.entity.Schedule;

import java.time.LocalDateTime;

public record ScheduleDto (
        Long id,
        String title,
        String content,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String spot,
        Long clubId,
        Long checkListId
) {
    public ScheduleDto(Schedule schedule) {
        this(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getContent(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getSpot(),
                schedule.getClub().getId(),
                schedule.getCheckList() != null ? schedule.getCheckList().getId() : null
        );
    }
}
