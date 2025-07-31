package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MyClubControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClubService clubService;
    @Autowired
    private ClubMemberService clubMemberService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ClubMemberRepository clubMemberRepository;


    @MockitoBean
    private S3Service s3Service; // S3Service는 MockBean으로 주입하여 실제 S3와의 통신을 피합니다
    @Test
    @DisplayName("모임 초대 수락")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );

        String jsonData = """
                {
                    "accept": true
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대를 수락했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증: 클럽 멤버 목록에 초대된 멤버가 포함되어 있는지 확인
        assertThat(club.getClubMembers().get(1).getMember().getId()).isEqualTo(invitedMember.getId());
        assertThat(club.getClubMembers().get(1).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
        assertThat(club.getClubMembers().get(1).getState()).isEqualTo(ClubMemberState.JOINING);
    }

    @Test
    @DisplayName("모임 초대 거절")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void rejectClubInvitation() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 멤버를 초대 (1번을 초대)
        Member invitedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("초대된 멤버가 존재하지 않습니다."));

        clubMemberService.addMemberToClub(
                club.getId(),
                invitedMember,
                ClubMemberRole.PARTICIPANT
        );

        String jsonData = """
                {
                    "accept": false
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/my-clubs/" + club.getId() + "/invitation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("rejectClubInvitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 초대를 거절했습니다."))
                .andExpect(jsonPath("$.data.clubId").value(club.getId()))
                .andExpect(jsonPath("$.data.clubName").value(club.getName()));

        // 추가 검증:
        assertThat(club.getClubMembers().size()).isEqualTo(1); // 초대된 멤버가 거절했으므로 클럽 멤버 수는 1명이어야 함
        assertThat(club.getClubMembers().get(0).getMember().getId()).isEqualTo(hostMember.getId());
    }

    @Test
    @DisplayName("모임 초대 수락 - 초대 상태가 아닌 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_NotInvited() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        String jsonData = """
                {
                    "accept": true
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("클럽 초대 상태가 아닙니다."));
    }

    @Test
    @DisplayName("모임 초대 수락 - 이미 가입 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_AlreadyJoined() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 이미 가입된 멤버 추가 (1번을 이미 가입 상태로 추가)
        Member alreadyJoinedMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("이미 가입된 멤버가 존재하지 않습니다."));

        ClubMember alreadyClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                alreadyJoinedMember,
                ClubMemberRole.PARTICIPANT
        );

        alreadyClubMember.updateState(ClubMemberState.JOINING); // 이미 가입 상태로 업데이트
        clubMemberRepository.save(alreadyClubMember);


        String jsonData = """
                {
                    "accept": true
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 가입 상태입니다."));
    }

    @Test
    @DisplayName("모임 초대 수락 - 이미 가입 신청 중인 경우 예외 발생")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_AlreadyApplying() throws Exception {
        // given
        Club club = clubService.createClub(
                Club.builder()
                        .name("테스트 그룹")
                        .bio("테스트 그룹 설명")
                        .category(ClubCategory.STUDY)
                        .mainSpot("서울")
                        .maximumCapacity(10)
                        .eventType(EventType.ONE_TIME)
                        .startDate(LocalDate.of(2023, 10, 1))
                        .endDate(LocalDate.of(2023, 10, 31))
                        .isPublic(true)
                        .leaderId(2L)
                        .build()
        );

        // 클럽에 호스트 멤버 추가 (2번을 호스트로)
        Member hostMember = memberService.findMemberById(2L)
                .orElseThrow(() -> new IllegalStateException("호스트 멤버가 존재하지 않습니다."));
        clubMemberService.addMemberToClub(
                club.getId(),
                hostMember,
                ClubMemberRole.HOST
        );

        // 클럽에 가입 신청 중인 멤버 추가 (1번을 가입 신청 상태로 추가)
        Member applyingMember = memberService.findMemberById(1L)
                .orElseThrow(() -> new IllegalStateException("가입 신청 중인 멤버가 존재하지 않습니다."));

        ClubMember applyingClubMember = clubMemberService.addMemberToClub(
                club.getId(),
                applyingMember,
                ClubMemberRole.PARTICIPANT
        );

        applyingClubMember.updateState(ClubMemberState.APPLYING); // 가입 신청 상태로 업데이트
        clubMemberRepository.save(applyingClubMember);

        String jsonData = """
                {
                    "accept": true
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/" + club.getId() + "/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value( "클럽 초대 상태가 아닙니다."));
    }

    // 잘못된 클럽
    @Test
    @DisplayName("잘못된 클럽 ID로 모임 초대 수락 시도")
    @WithUserDetails(value = "hgd222@test.com") // 1번 멤버로 로그인
    public void acceptClubInvitation_InvalidClubId() throws Exception {
        // given
        String jsonData = """
                {
                    "accept": true
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/my-clubs/999/join") // 존재하지 않는 클럽 ID
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1MyClubController.class))
                .andExpect(handler().methodName("acceptClubInvitation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }


}