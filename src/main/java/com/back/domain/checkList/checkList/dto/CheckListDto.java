package com.back.domain.checkList.checkList.dto;

import com.back.domain.checkList.checkList.entity.CheckList;
import com.back.domain.preset.preset.entity.Preset;
import com.back.domain.schedule.schedule.dto.ScheduleDto;

import java.util.List;

public record CheckListDto(
    Long id,
    boolean isActive,
    ScheduleDto schedule,
    List<CheckListItemDto> checkListItems
) {
  public CheckListDto(CheckList checkList) {
    this(
        checkList.getId(),
        checkList.isActive(),
        new ScheduleDto(checkList.getSchedule()),
        checkList.getCheckListItems().stream()
            .map(CheckListItemDto::new)
            .toList()
    );
  }
}
