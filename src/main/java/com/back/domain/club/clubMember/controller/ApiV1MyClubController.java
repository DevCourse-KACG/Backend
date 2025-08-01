package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.clubMember.dtos.MyClubControllerDtos;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.service.MyClubService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 클럽 관련 API를 제공하는 컨트롤러
 * 유저 본인 입장에서 클럽 멤버 정보를 관리하는 기능을 포함한다
 */
@RestController
@RequestMapping("/api/v1/my-clubs")
@RequiredArgsConstructor
@Tag(name = "MyClubController", description = "유저 본인 클럽 관련 API")
public class ApiV1MyClubController {
    private final MyClubService myClubService;


    @GetMapping("{clubId}")
    @Operation(summary = "클럽에서 내 정보 조회")
    public RsData<MyClubControllerDtos.MyInfoInClub> getMyClubInfo(
            @PathVariable Long clubId
    ) {
        // 클럽 멤버 정보를 조회하는 서비스 메서드를 호출
        ClubMember clubMember = myClubService.getMyClubInfo(clubId);

        // 조회된 클럽 멤버 정보를 응답으로 반환
        return RsData.of(200,
                "클럽 멤버 정보를 조회했습니다.",
                new MyClubControllerDtos.MyInfoInClub(
                        clubMember.getId(),
                        clubMember.getClub().getId(),
                        clubMember.getClub().getName(),
                        clubMember.getRole(),
                        clubMember.getState()
                )
        );
    }


    @PatchMapping("{clubId}/join")
    @Operation(summary = "클럽 초대 수락")
    public RsData<MyClubControllerDtos.SimpleClubInfo> acceptClubInvitation(
            @PathVariable Long clubId
    ) {
        // 클럽 초대 수락 로직을 처리하는 서비스 메서드를 호출
        Club selectedClub = myClubService.handleClubInvitation(clubId, true);

        // 성공적으로 초대를 수락/거절한 경우 응답 반환
        return RsData.of(
                200,
                "클럽 초대를 수락했습니다.",
                new MyClubControllerDtos.SimpleClubInfo(
                        selectedClub.getId(),
                        selectedClub.getName()
                )
        );
    }

    @DeleteMapping("{clubId}/invitation")
    @Operation(summary = "클럽 초대 거절")
    public RsData<MyClubControllerDtos.SimpleClubInfo> rejectClubInvitation(
            @PathVariable Long clubId
    ) {
        // 클럽 초대 거절 로직을 처리하는 서비스 메서드를 호출
        Club selectedClub = myClubService.handleClubInvitation(clubId, false);

        // 성공적으로 초대를 수락/거절한 경우 응답 반환
        return RsData.of(
                200,
                "클럽 초대를 거절했습니다.",
                new MyClubControllerDtos.SimpleClubInfo(
                        selectedClub.getId(),
                        selectedClub.getName()
                )
        );
    }

    @PostMapping("{clubId}/apply")
    @Operation(summary = "클럽 가입 신청")
    public RsData<MyClubControllerDtos.SimpleClubInfo> applyForPublicClub(
            @PathVariable Long clubId
    ) {
        // 클럽 가입 신청 로직을 처리하는 서비스 메서드를 호출
        Club selectedClub = myClubService.applyForClub(clubId);

        // 성공적으로 클럽 가입 신청을 한 경우 응답 반환
        return RsData.of(
                200,
                "클럽 가입 신청을 완료했습니다.",
                new MyClubControllerDtos.SimpleClubInfo(
                        selectedClub.getId(),
                        selectedClub.getName()
                )
        );
    }


}
