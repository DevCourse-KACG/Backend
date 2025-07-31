package com.back.domain.club.clubMember.dtos;

import jakarta.validation.constraints.NotNull;

public class MyClubControllerDtos {
    /**
     * 클럽 초대 수락 요청 DTO
     * accept: true면 초대를 수락, false면 거절
     */
    public static record AcceptClubInvitationRequest(
            @NotNull
            Boolean accept
    ) {
    }

    /**
     * 클럽 초대 수락 응답 DTO
     * 클럽 ID와 클럽 이름을 포함
     */
    public static record AcceptClubInvitationResponse(
            Long clubId,
            String clubName
    ) {
    }
}
