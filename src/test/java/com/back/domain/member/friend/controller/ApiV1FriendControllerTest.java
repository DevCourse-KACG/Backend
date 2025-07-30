package com.back.domain.member.friend.controller;

import com.back.domain.member.friend.repository.FriendRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiV1FriendControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FriendRepository friendRepository;

    void performErrAddFriend(String friendEmail,
                          int expectedStatus,
                          String expectedMessage) throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/v1/members/me/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"friend_email": "%s"}
                                """.formatted(friendEmail)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("addFriend"))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @DisplayName("친구 추가")
    @WithUserDetails(value = "hgd222@test.com")
    void tc1() throws Exception {
        Long friendId = 2L;
        String friendEmail = "chs4s@test.com";
        Member friend = memberRepository.findById(friendId).orElseThrow();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/members/me/friends")
                        .contentType("application/json")
                        .content("""
                                {
                                    "friend_email": "chs4s@test.com"
                                }
                                """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("addFriend"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%s 에게 친구 추가 요청이 성공적으로 처리되었습니다.".formatted(friendEmail)))
                .andExpect(jsonPath("$.data.friendId").value(friend.getId()))
                .andExpect(jsonPath("$.data.friendNickname").value(friend.getNickname()))
                .andExpect(jsonPath("$.data.friendBio").value(friend.getMemberInfo().getBio()))
                .andExpect(jsonPath("$.data.friendProfileImageUrl").value(friend.getMemberInfo().getProfileImageUrl()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("친구 추가 - 친구 대상이 없는 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void tc2() throws Exception {
        performErrAddFriend(
                "a@test.com",
                404,
                "친구 대상이 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("친구 추가 - 자기 자신을 친구로 추가하는 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void tc3() throws Exception {
        performErrAddFriend(
                "hgd222@test.com",
                400,
                "자기 자신을 친구로 추가할 수 없습니다."
        );
    }

    @Test
    @DisplayName("친구 추가 - 이미 친구 요청을 보낸 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void tc4() throws Exception {
        performErrAddFriend(
                "lyh3@test.com",
                409,
                "이미 친구 요청을 보냈습니다. 상대방의 수락을 기다려주세요."
        );
    }

    @Test
    @DisplayName("친구 추가 - 이미 친구 요청을 받은 경우 예외 처리")
    @WithUserDetails(value = "lyh3@test.com")
    void tc5() throws Exception {
        performErrAddFriend(
                "hgd222@test.com",
                409,
                "이미 친구 요청을 받았습니다. 수락 또는 거절해주세요."
        );
    }

    @Test
    @DisplayName("친구 추가 - 이미 친구인 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void tc6() throws Exception {
        performErrAddFriend(
                "cjw5@test.com",
                409,
                "이미 친구입니다."
        );
    }

    @Test
    @DisplayName("친구 추가 - 이전에 거절 당한 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void tc7() throws Exception {
        performErrAddFriend(
                "pms4@test.com",
                409,
                "이전에 거절한 친구 요청입니다. 다시 요청할 수 없습니다."
        );
    }
}