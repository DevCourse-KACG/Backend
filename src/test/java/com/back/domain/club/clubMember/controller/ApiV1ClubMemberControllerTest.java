package com.back.domain.club.clubMember.controller;

import com.back.domain.club.club.controller.ApiV1ClubController;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.service.ClubService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    private MemberService memberService;

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
                .andExpect(handler().handlerType(ApiV1ClubController.class))
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

}