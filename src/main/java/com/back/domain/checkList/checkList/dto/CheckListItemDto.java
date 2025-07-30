package com.back.domain.checkList.checkList.dto;

import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.checkList.itemAssign.dto.ItemAssignDto;
import com.back.domain.preset.preset.entity.PresetItem;
import com.back.global.enums.CheckListItemCategory;

import java.util.List;

public record CheckListItemDto(
    Long id,
    String content,
    CheckListItemCategory category,
    int sequence,
    boolean isChecked,
    List<ItemAssignDto> itemAssigns
) {
  public CheckListItemDto(CheckListItem checkListItem) {
    this(
        checkListItem.getId(),
        checkListItem.getContent(),
        checkListItem.getCategory(),
        checkListItem.getSequence(),
        checkListItem.isChecked(),
        checkListItem.getItemAssigns().stream()
            .map(ItemAssignDto::new)
            .toList()
    );
  }
}
