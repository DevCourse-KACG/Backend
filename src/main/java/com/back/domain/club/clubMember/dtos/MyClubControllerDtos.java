package com.back.domain.club.clubMember.dtos;

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
}
