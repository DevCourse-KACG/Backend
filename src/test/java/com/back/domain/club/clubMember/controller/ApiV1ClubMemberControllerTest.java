package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.club.clubMember.repository.ClubMemberRepository;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.MemberType;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1ClubMemberControllerTest {
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
    private S3Service s3Service; // S3Service는 MockBean으로 주입하여 실제 S3와의 통신을 피합니다.


    @Test
    @DisplayName("클럽에 멤버 추가")
    void addMemberToClub() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        Member member2 = memberService.findById(3L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // JSON 데이터 파트 생성
        String jsonData = """
                        {
                            "members": [
                                {
                                    "email": "%s",
                                    "role": "PARTICIPANT"
                                },
                                {
                                    "email": "%s",
                                    "role": "PARTICIPANT"
                                }
                            ]
                        }
                        """.stripIndent().formatted(member1.getEmail(), member2.getEmail());

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/clubs/" + club.getId() + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("addMembersToClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽에 멤버가 추가됐습니다."));

        // 추가 검증: 클럽에 멤버가 실제로 추가되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getClubMembers().size()).isEqualTo(2); // 멤버가 2명 추가되었는지 확인
        assertThat(club.getClubMembers().get(0).getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(club.getClubMembers().get(0).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
        assertThat(club.getClubMembers().get(1).getMember().getEmail()).isEqualTo(member2.getEmail());
        assertThat(club.getClubMembers().get(1).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
    }

    @Test
    @DisplayName("클럽에 멤버 추가 - 중복되는 멤버")
    void addMemberToClub_DuplicateMember() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // JSON 데이터 파트 생성
        String jsonData = """
                {
                    "members": [
                        {
                            "email": "%s",
                            "role": "PARTICIPANT"
                        },
                        {
                            "email": "%s",
                            "role": "MANAGER"
                        }
                    ]
                }
                """.stripIndent().formatted(member1.getEmail(), member1.getEmail());

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/clubs/" + club.getId() + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("addMembersToClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽에 멤버가 추가됐습니다."));

        // 추가 검증: 클럽에 멤버가 실제로 추가되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 중복된 멤버는 하나만 추가
        assertThat(club.getClubMembers().get(0).getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(club.getClubMembers().get(0).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
    }

    @Test
    @DisplayName("클럽에 멤버 추가 - 이미 추가된 멤버")
    void addMemberToClub_AlreadyAddedMember() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );



        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 클럽에 멤버 추가
        clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 클럽에 멤버가 1명 추가되었는지 확인

        // JSON 데이터 파트 생성
        String jsonData = """
                {
                    "members": [
                        {
                            "email": "%s",
                            "role": "PARTICIPANT"
                        }
                    ]
                }
                """.stripIndent().formatted(member1.getEmail());

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/clubs/" + club.getId() + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("addMembersToClub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("클럽에 멤버가 추가됐습니다."));

        // 추가 검증: 클럽에 멤버가 실제로 추가되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getClubMembers().size()).isEqualTo(1);
        assertThat(club.getClubMembers().get(0).getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(club.getClubMembers().get(0).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
    }

    @Test
    @DisplayName("클럽에 멤버 추가 - 존재하지 않는 클럽")
    void addMemberToClub_ClubNotFound() throws Exception {
        // given
        String nonExistentClubId = "9999"; // 존재하지 않는 클럽 ID
        String jsonData = """
                {
                    "members": [
                        {
                            "email": "test1@gmail.com",
                            "role": "PARTICIPANT"
                        }
                    ]
                }
                """.stripIndent();
        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/clubs/" + nonExistentClubId + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("addMembersToClub"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("클럽에 멤버 추가 - 존재하지 않는 멤버 이메일")
    void addMemberToClub_MemberNotFound() throws Exception {
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
                        .leaderId(1L)
                        .build()
        );

        String jsonData = """
                {
                    "members": [
                        {
                            "email": "unknownMember@gmail.com",
                            "role": "PARTICIPANT"
                        }
                    ]
                }
                """.stripIndent();

        // when
        ResultActions resultActions = mvc.perform(
                        post("/api/v1/clubs/" + club.getId() + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("addMembersToClub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("클럽 멤버 탈퇴")
    void withdrawMemberFromClub() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 클럽에 멤버 추가
        clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 클럽에 멤버가 1명 추가되었는지 확인

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/clubs/" + club.getId() + "/members/" + member1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("withdrawMemberFromClub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽에서 멤버가 탈퇴됐습니다."));

        // 추가 검증: 클럽에서 멤버가 실제로 삭제되지 않고 state가 withdrawn로 변경되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 클럽에 멤버가 여전히 존재해야 함
        assertThat(club.getClubMembers().get(0).getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(club.getClubMembers().get(0).getRole()).isEqualTo(ClubMemberRole.PARTICIPANT);
        assertThat(club.getClubMembers().get(0).getState()).isEqualTo(ClubMemberState.WITHDRAWN); // 상태가 WITHDRAWN으로 변경되었는지 확인
    }

    @Test
    @DisplayName("클럽 멤버 탈퇴 - 존재하지 않는 클럽")
    void withdrawMemberFromClub_ClubNotFound() throws Exception {
        // given
        String nonExistentClubId = "9999"; // 존재하지 않는 클럽 ID
        Long memberId = 2L; // 임의의 멤버 ID

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/clubs/" + nonExistentClubId + "/members/" + memberId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("withdrawMemberFromClub"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("클럽 멤버 탈퇴 - 멤버가 클럽에 존재하지 않을 때")
    void withdrawMemberFromClub_MemberNotFound() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        Long nonExistentMemberId = 9999L; // 존재하지 않는 멤버 ID

        // when
        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/clubs/" + club.getId() + "/members/" + nonExistentMemberId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("withdrawMemberFromClub"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("멤버가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("참여자 권한 변경")
    void changeMemberRole() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 클럽에 멤버 추가
        clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 클럽에 멤버가 1명 추가되었는지 확인

        // when
        ResultActions resultActions = mvc.perform(
                        put("/api/v1/clubs/" + club.getId() + "/members/" + member1.getId() + "/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"MANAGER\"}")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("changeMemberRole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("멤버의 권한이 변경됐습니다."));

        // 추가 검증: 클럽에서 멤버의 역할이 실제로 변경되었는지 확인
        club = clubService.getClubById(club.getId()).orElseThrow(
                () -> new IllegalStateException("클럽이 존재하지 않습니다.")
        );

        assertThat(club.getClubMembers().size()).isEqualTo(1);
        assertThat(club.getClubMembers().get(0).getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(club.getClubMembers().get(0).getRole()).isEqualTo(ClubMemberRole.MANAGER); // 역할이 MANAGER로 변경되었는지 확인
    }

    @Test
    @DisplayName("참여자 권한 변경 - 존재하지 않는 클럽")
    void changeMemberRole_ClubNotFound() throws Exception {
        // given
        String nonExistentClubId = "9999"; // 존재하지 않는 클럽 ID
        Long memberId = 2L; // 임의의 멤버 ID

        // when
        ResultActions resultActions = mvc.perform(
                        put("/api/v1/clubs/" + nonExistentClubId + "/members/" + memberId + "/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"MANAGER\"}")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("changeMemberRole"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("참여자 권한 변경 - 멤버가 클럽에 존재하지 않을 때")
    void changeMemberRole_MemberNotFound() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        Long nonExistentMemberId = 9999L; // 존재하지 않는 멤버 ID

        // when
        ResultActions resultActions = mvc.perform(
                        put("/api/v1/clubs/" + club.getId() + "/members/" + nonExistentMemberId + "/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"MANAGER\"}")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("changeMemberRole"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("멤버가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("참여자 권한 변경 - 잘못된 역할 요청")
    void changeMemberRole_InvalidRole() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 클럽에 멤버 추가
        clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(1); // 클럽에 멤버가 1명 추가되었는지 확인

        // when
        ResultActions resultActions = mvc.perform(
                        put("/api/v1/clubs/" + club.getId() + "/members/" + member1.getId() + "/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"INVALID_ROLE\"}")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("changeMemberRole"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Unknown Member role: INVALID_ROLE"));
    }

    @Test
    @DisplayName("참여자 목록 반환 - state 필터 없음")
    void getClubMembers() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        Member member2 = memberService.findById(3L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 비회원 멤버 추가
        Member nonMember = Member.builder()
                .id(4L)
                .nickname("비회원")
                .password("password")
                .memberType(MemberType.GUEST)
                .tag("123456")
                .build();

        // 클럽에 멤버 추가
        ClubMember clubMember1 = clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);
        ClubMember clubMember2 = clubMemberService.addMemberToClub(club.getId(), member2, ClubMemberRole.MANAGER);
        ClubMember nonMemberClubMember = clubMemberService.addMemberToClub(club.getId(), nonMember, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(3); // 클럽에 멤버가 2명 추가되었는지 확인

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/clubs/" + club.getId() + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("getClubMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 멤버 목록이 조회됐습니다."))
                .andExpect(jsonPath("$.data.members.length()").value(3)) // 멤버가 3명인지 확인

                .andExpect(jsonPath("$.data.members[0].clubMemberId").value(clubMember1.getId()))
                .andExpect(jsonPath("$.data.members[0].memberId").value(member1.getId()))
                .andExpect(jsonPath("$.data.members[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.members[0].tag").value(member1.getTag()))
                .andExpect(jsonPath("$.data.members[0].role").value(ClubMemberRole.PARTICIPANT.name()))
                .andExpect(jsonPath("$.data.members[0].email").value(member1.getEmail()))
                .andExpect(jsonPath("$.data.members[0].memberType").value(member1.getMemberType().name()))
                .andExpect(jsonPath("$.data.members[0].profileImageUrl").value(""))
                .andExpect(jsonPath("$.data.members[0].state").value(clubMember1.getState().name()))

                .andExpect(jsonPath("$.data.members[1].clubMemberId").value(clubMember2.getId()))
                .andExpect(jsonPath("$.data.members[1].memberId").value(member2.getId()))
                .andExpect(jsonPath("$.data.members[1].nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.members[1].tag").value(member2.getTag()))
                .andExpect(jsonPath("$.data.members[1].role").value(ClubMemberRole.MANAGER.name()))
                .andExpect(jsonPath("$.data.members[1].email").value(member2.getEmail()))
                .andExpect(jsonPath("$.data.members[1].memberType").value(member2.getMemberType().name()))
                .andExpect(jsonPath("$.data.members[1].profileImageUrl").value(""))
                .andExpect(jsonPath("$.data.members[1].state").value(clubMember2.getState().name()))

                .andExpect(jsonPath("$.data.members[2].clubMemberId").value(nonMemberClubMember.getId()))
                .andExpect(jsonPath("$.data.members[2].memberId").value(nonMember.getId()))
                .andExpect(jsonPath("$.data.members[2].nickname").value(nonMember.getNickname()))
                .andExpect(jsonPath("$.data.members[2].tag").value(nonMember.getTag()))
                .andExpect(jsonPath("$.data.members[2].role").value(ClubMemberRole.PARTICIPANT.name()))
                .andExpect(jsonPath("$.data.members[2].email").value("")) // 비회원은 이메일이 없으므로 빈 문자열
                .andExpect(jsonPath("$.data.members[2].memberType").value(nonMember.getMemberType().name()))
                .andExpect(jsonPath("$.data.members[2].profileImageUrl").value("")) // 비회원은 이미지 URL이 없으므로 빈 문자열
                .andExpect(jsonPath("$.data.members[2].state").value(nonMemberClubMember.getState().name())); // 비회원의 상태 확인
    }

    @Test
    @DisplayName("참여자 목록 반환 - state 필터 (INVITED)")
    void getClubMembers_stateFiltered() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // 추가할 멤버 (testInitData의 멤버 사용)
        Member member1 = memberService.findById(2L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        Member member2 = memberService.findById(3L).orElseThrow(
                () -> new IllegalStateException("멤버가 존재하지 않습니다.")
        );

        // 비회원 멤버 추가
        Member nonMember = Member.builder()
                .id(4L)
                .nickname("비회원")
                .password("password")
                .memberType(MemberType.GUEST)
                .tag("123456")
                .build();

        // 클럽에 멤버 추가
        ClubMember clubMember1 = clubMemberService.addMemberToClub(club.getId(), member1, ClubMemberRole.PARTICIPANT);
        ClubMember clubMember2 = clubMemberService.addMemberToClub(club.getId(), member2, ClubMemberRole.MANAGER);
        ClubMember nonMemberClubMember = clubMemberService.addMemberToClub(club.getId(), nonMember, ClubMemberRole.PARTICIPANT);

        assertThat(club.getClubMembers().size()).isEqualTo(3); // 클럽에 멤버가 2명 추가되었는지 확인

        // 클럽 멤버의 상태 변경
        clubMember2.updateState(ClubMemberState.JOINING); // member2를 JOINING 상태로 변경
        clubMemberRepository.save(clubMember2); // 상태 변경된 클럽 멤버 저장

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/clubs/" + club.getId() + "/members" + "?state=INVITED")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("getClubMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("클럽 멤버 목록이 조회됐습니다."))
                .andExpect(jsonPath("$.data.members.length()").value(2)) // 멤버가 2명인지 확인

                .andExpect(jsonPath("$.data.members[0].clubMemberId").value(clubMember1.getId()))
                .andExpect(jsonPath("$.data.members[0].memberId").value(member1.getId()))
                .andExpect(jsonPath("$.data.members[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.members[0].tag").value(member1.getTag()))
                .andExpect(jsonPath("$.data.members[0].role").value(ClubMemberRole.PARTICIPANT.name()))
                .andExpect(jsonPath("$.data.members[0].email").value(member1.getEmail()))
                .andExpect(jsonPath("$.data.members[0].memberType").value(member1.getMemberType().name()))
                .andExpect(jsonPath("$.data.members[0].profileImageUrl").value(""))
                .andExpect(jsonPath("$.data.members[0].state").value(clubMember1.getState().name()))

                .andExpect(jsonPath("$.data.members[1].clubMemberId").value(nonMemberClubMember.getId()))
                .andExpect(jsonPath("$.data.members[1].memberId").value(nonMember.getId()))
                .andExpect(jsonPath("$.data.members[1].nickname").value(nonMember.getNickname()))
                .andExpect(jsonPath("$.data.members[1].tag").value(nonMember.getTag()))
                .andExpect(jsonPath("$.data.members[1].role").value(ClubMemberRole.PARTICIPANT.name()))
                .andExpect(jsonPath("$.data.members[1].email").value("")) // 비회원은 이메일이 없으므로 빈 문자열
                .andExpect(jsonPath("$.data.members[1].memberType").value(nonMember.getMemberType().name()))
                .andExpect(jsonPath("$.data.members[1].profileImageUrl").value("")) // 비회원은 이미지 URL이 없으므로 빈 문자열
                .andExpect(jsonPath("$.data.members[1].state").value(nonMemberClubMember.getState().name())); // 비회원의 상태 확인
    }

    @Test
    @DisplayName("참여자 목록 반환 - 존재하지 않는 클럽")
    void getClubMembers_InvalidClubId() throws Exception {
        // given
        int invalidClubId = 9999; // 잘못된 클럽 ID

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/clubs/" + invalidClubId + "/members")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("getClubMembers"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("클럽이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("참여자 목록 반환 - 잘못된 state 필터")
    void getClubMembers_InvalidStateFilter() throws Exception {
        // given
        // 테스트 클럽 생성
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
                        .leaderId(1L)
                        .build()
        );

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/clubs/" + club.getId() + "/members?state=INVALID_STATE")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(ApiV1ClubMemberController.class))
                .andExpect(handler().methodName("getClubMembers"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Unknown Member state: INVALID_STATE"));
    }
}

