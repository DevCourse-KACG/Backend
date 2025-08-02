package com.back.domain.checkList.itemAssign.dto;

import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.club.clubMember.entity.ClubMember;

public record ItemAssignDto(
    Long id,
    String clubMemberName,
    boolean isChecked
) {
  public ItemAssignDto(ItemAssign itemAssign) {
    this(
        itemAssign.getId(),
        itemAssign.getClubMember().getMember().getNickname(),
        itemAssign.isChecked()
    );

  }
}
