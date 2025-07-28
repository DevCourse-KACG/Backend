package com.back.domain.club.club.controller;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
@Tag(name = "ClubController", description = "클럽 관련 API")
public class ApiV1ClubController {
    private final ClubService clubService;

    @PostMapping
    @Operation(summary = "클럽 생성")
    public RsData<ClubControllerDtos.CreateClubResponse> createClub(@Valid @RequestBody ClubControllerDtos.CreateClubRequest reqBody) {
        Club club = clubService.createClub(reqBody);

        return new RsData<>(201, "클럽이 생성됐습니다.",
                new ClubControllerDtos.CreateClubResponse(
                        club.getId(),
                        club.getLeaderId()
                )
        );
    }


}
