package com.back.domain.club.clubMember.dtos;

import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;

public class MyClubControllerDtos {

    /**
     * 클럽 초대 수락 응답 DTO
     * 클럽 ID와 클럽 이름을 포함
     */
    public static record SimpleClubInfo(
            Long clubId,
            String clubName
    ) {
    }

    /**
     * 클럽 내 내 정보 조회 응답 DTO
     * 클럽 멤버 ID, 클럽 ID, 클럽 이름, 역할, 상태를 포함
     */
    public static record MyInfoInClub(
            Long clubMemberId,
            Long clubId,
            String clubName,
            ClubMemberRole role,
            ClubMemberState state
    ){}
}
