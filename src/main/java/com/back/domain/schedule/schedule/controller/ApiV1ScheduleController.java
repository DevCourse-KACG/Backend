package com.back.domain.schedule.schedule.controller;

import com.back.domain.schedule.schedule.dto.ScheduleCreateReqBody;
import com.back.domain.schedule.schedule.dto.ScheduleDto;
import com.back.domain.schedule.schedule.dto.ScheduleUpdateReqBody;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.domain.schedule.schedule.service.ScheduleService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "ApiV1ScheduleController", description = "일정 컨트롤러")
public class ApiV1ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 조회")
    public RsData<ScheduleDto> getSchedule(
            @PathVariable Long scheduleId
    ) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return RsData.of(
                200,
                "%s번 일정이 조회되었습니다.".formatted(scheduleId),
                new ScheduleDto(schedule)
        );
    }

    @PostMapping
    @Operation(summary = "일정 생성")
    public RsData<ScheduleDto> createSchedule(
            @Valid @RequestBody ScheduleCreateReqBody reqBody
    ) {
        Schedule schedule = scheduleService.createSchedule(reqBody);

        return RsData.of(
                201,
                "%s번 일정이 생성되었습니다.".formatted(schedule.getId()),
                new ScheduleDto(schedule)
        );
    }

    @PutMapping("{scheduleId}")
    @Operation(summary = "일정 수정")
    public RsData<ScheduleDto> modifySchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateReqBody reqBody
    ) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        scheduleService.modifySchedule(schedule, reqBody);

        return RsData.of(
                200,
                "%s번 일정이 수정되었습니다.".formatted(schedule.getId()),
                new ScheduleDto(schedule)
        );
    }

    @DeleteMapping("{scheduleId}")
    @Operation(summary = "일정 삭제")
    public RsData<Void> deleteSchedule(
            @PathVariable Long scheduleId
    ) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        scheduleService.deleteSchedule(schedule);

        return RsData.of(
                200,
                "%s번 일정이 삭제되었습니다.".formatted(scheduleId)
        );
    }
}
