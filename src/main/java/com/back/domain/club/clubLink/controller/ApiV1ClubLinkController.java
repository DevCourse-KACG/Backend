package com.back.domain.club.clubLink.controller;

import com.back.domain.club.clubLink.dtos.ClubLinkDtos;
import com.back.domain.club.clubLink.service.ClubLinkService;
import com.back.domain.member.member.entity.Member;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class ApiV1ClubLinkController {
    private final Rq rq;
    private final ClubLinkService clubLinkService;

    @PostMapping("/{clubId}/members/invitation-link")
    @Operation(summary = "클럽 초대 링크 생성")
    public RsData<ClubLinkDtos.CreateClubLinkResponse> createClubLink(@PathVariable @Positive Long clubId) {

        Member user = rq.getActor();
        ClubLinkDtos.CreateClubLinkResponse response = clubLinkService.createClubLink(user, clubId);

        return new RsData<>(200, "클럽 초대 링크가 생성되었습니다.", response);
    }

    @GetMapping("/{clubId}/members/invitation-link")
    @Operation(summary = "클럽 초대 링크 반환")
    public RsData<ClubLinkDtos.CreateClubLinkResponse> getExistingClubLink(@PathVariable @Positive Long clubId) {

        Member user = rq.getActor();
        ClubLinkDtos.CreateClubLinkResponse response = clubLinkService.getExistingClubLink(user, clubId);

        return new RsData<>(200, "클럽 초대 링크가 반환되었습니다.", response);
    }

    @PostMapping("/invitations/{token}/apply")
    @Operation(summary = "로그인 유저 - 초대 링크를 통한 비공개 클럽 가입 신청")
    public RsData<Object> applyToClubByInvitationToken(@PathVariable @Positive String token) {
        Member user = rq.getActor();

        return clubLinkService.applyToPrivateClubByToken(user, token);
    }
}
