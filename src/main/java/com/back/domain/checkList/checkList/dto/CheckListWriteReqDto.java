package com.back.domain.checkList.checkList.dto;

import com.back.domain.schedule.schedule.dto.ScheduleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckListWriteReqDto(
    @NotNull(message = "일정 ID는 필수입니다.")
    Long scheduleId,
    boolean isActive,
    @Valid
    List<CheckListItemWriteReqDto> checkListItems
) {
}
