package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.club.clubMember.service.ClubMemberService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("클럽에 멤버 추가 - 클럽이 존재하지 않을 때")
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
        assertThat(club.getClubMembers().get(0).getState()).isEqualTo("WITHDRAWN"); // 상태가 WITHDRAWN으로 변경되었는지 확인
    }

    @Test
    @DisplayName("클럽 멤버 탈퇴 - 클럽이 존재하지 않을 때")
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }
}

