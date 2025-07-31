package com.back.domain.member.friend.controller;

import com.back.domain.member.friend.dto.FriendDto;
import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.repository.FriendRepository;
import com.back.domain.member.friend.service.FriendService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private FriendService friendService;
    @Autowired
    private FriendRepository friendRepository;

    /**
     * 친구 추가 요청 예외 처리하는 메서드
     * @param friendEmail       친구의 이메일
     * @param expectedStatus    예상되는 HTTP 상태 코드
     * @param expectedMessage   예상되는 에러 메시지
     */
    void performErrAddFriend(String friendEmail,
                             int expectedStatus,
                             String expectedMessage
    ) throws Exception {
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

    /**
     * 친구 요청 수락, 거절 예외 처리하는 메서드
     * @param friendId          친구 엔티티 ID
     * @param pathUrl           URL 경로 (accept, reject 등)
     * @param methodName        메서드 이름
     * @param expectedStatus    예상되는 HTTP 상태 코드
     * @param expectedMessage   예상되는 에러 메시지
     */
    void performErrPatchFriend(Long friendId,
                               String pathUrl,
                               String methodName,
                               int expectedStatus,
                               String expectedMessage
    ) throws Exception {
        ResultActions resultActions = mockMvc
                .perform(patch("/api/v1/members/me/friends/%d/%s".formatted(friendId, pathUrl))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName(methodName))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    /**
     * 친구 삭제 요청 예외 처리하는 메서드
     * @param friendId          친구 엔티티 ID
     * @param expectedStatus    예상되는 HTTP 상태 코드
     * @param expectedMessage   예상되는 에러 메시지
     */
    void performErrDelFriend(Long friendId,
                             int expectedStatus,
                             String expectedMessage
    ) throws Exception {
        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/members/me/friends/" + friendId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("deleteFriend"))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @DisplayName("친구 목록 조회")
    @WithUserDetails(value = "hgd222@test.com")
    void trl1() throws Exception {
        Long memberId = 1L;

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/members/me/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        List<FriendDto> friends = friendService.getFriends(memberId);

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("getFriends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("친구 목록을 성공적으로 조회하였습니다."));

        for (int i = 0; i < friends.size(); i++) {
            FriendDto friend = friends.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].friendId".formatted(i)).value(friend.friendId()))
                    .andExpect(jsonPath("$.data[%d].friendMemberId".formatted(i)).value(friend.friendMemberId()))
                    .andExpect(jsonPath("$.data[%d].friendNickname".formatted(i)).value(friend.friendNickname()))
                    .andExpect(jsonPath("$.data[%d].friendProfileImageUrl".formatted(i)).value(friend.friendProfileImageUrl()))
                    .andExpect(jsonPath("$.data[%d].status".formatted(i)).value("ACCEPTED"));
        }
    }

    @Test
    @DisplayName("친구 추가")
    @WithUserDetails(value = "hgd222@test.com")
    void tc1() throws Exception {
        // 친구 요청 받은 회원
        Long friendMemberId = 2L;
        Member friendMember = memberRepository.findById(friendMemberId).orElseThrow();
        String friendEmail = friendMember.getMemberInfo().getEmail();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/members/me/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"friend_email": "%s"}
                                """.formatted(friendEmail))
                )
                .andDo(print());

        // 로그인 회원(친구 요청 보낸 회원)
        Member me = memberRepository.findById(1L).orElseThrow();
        // 친구 엔티티
        Friend friend = friendRepository.findFirstByRequestedByOrderByIdDesc(me).orElseThrow();

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("addFriend"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%s 에게 친구 추가 요청이 성공적으로 처리되었습니다.".formatted(friendEmail)))
                .andExpect(jsonPath("$.data.friendId").value(friend.getId()))
                .andExpect(jsonPath("$.data.friendMemberId").value(friendMember.getId()))
                .andExpect(jsonPath("$.data.friendNickname").value(friendMember.getNickname()))
                .andExpect(jsonPath("$.data.friendBio").value(friendMember.getMemberInfo().getBio()))
                .andExpect(jsonPath("$.data.friendProfileImageUrl").value(friendMember.getMemberInfo().getProfileImageUrl()))
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

    @Test
    @DisplayName("친구 수락")
    @WithUserDetails(value = "lyh3@test.com")
    void ta1() throws Exception {
        // 친구 엔티티
        Long friendId = 1L;
        Friend friend = friendRepository.findById(friendId).orElseThrow();

        ResultActions resultActions = mockMvc
                .perform(patch("/api/v1/members/me/friends/%d/accept".formatted(friendId))
                )
                .andDo(print());

        // 친구 요청한 회원
        Member friendMember = friend.getRequestedBy();
        MemberInfo friendMemberInfo = friendMember.getMemberInfo();

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("acceptFriend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s님과 친구가 되었습니다.".formatted(friendMember.getNickname())))
                .andExpect(jsonPath("$.data.friendId").value(friend.getId()))
                .andExpect(jsonPath("$.data.friendNickname").value(friendMember.getNickname()))
                .andExpect(jsonPath("$.data.friendBio").value(friendMemberInfo.getBio()))
                .andExpect(jsonPath("$.data.friendProfileImageUrl").value(friendMemberInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("친구 수락 - 친구 요청이 없는 경우 예외 처리")
    @WithUserDetails(value = "lyh3@test.com")
    void ta2() throws Exception {
        performErrPatchFriend(
                100L,
                "accept",
                "acceptFriend",
                404,
                "친구 요청이 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("친구 수락 - 친구 엔티티는 있으나 로그인 회원과 관련 없을 경우 예외 처리")
    @WithUserDetails(value = "chs4s@test.com")
    void ta3() throws Exception {
        performErrPatchFriend(
                1L,
                "accept",
                "acceptFriend",
                400,
                "로그인한 회원과 관련된 친구가 아닙니다."
        );
    }

    @Test
    @DisplayName("친구 수락 - 받는이가 아닌 요청자가 친구 요청을 수락하는 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void ta4() throws Exception {
        performErrPatchFriend(
                1L,
                "accept",
                "acceptFriend",
                400,
                "요청한 사람이 친구 수락할 수 없습니다. 친구에게 요청 수락을 받으세요."
        );
    }

    @Test
    @DisplayName("친구 수락 - 이미 친구인 경우 예외 처리")
    @WithUserDetails(value = "cjw5@test.com")
    void ta5() throws Exception {
        performErrPatchFriend(
                2L,
                "accept",
                "acceptFriend",
                400,
                "이미 친구입니다."
        );
    }

    @Test
    @DisplayName("친구 요청 거절")
    @WithUserDetails(value = "lyh3@test.com")
    void trj1() throws Exception {
        // 친구 엔티티
        Long friendId = 1L;
        Friend friend = friendRepository.findById(friendId).orElseThrow();

        ResultActions resultActions = mockMvc
                .perform(patch("/api/v1/members/me/friends/%d/reject".formatted(friendId))
                )
                .andDo(print());

        // 친구 요청한 회원
        Member friendMember = friend.getRequestedBy();
        MemberInfo friendMemberInfo = friendMember.getMemberInfo();

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("rejectFriend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s님의 친구 요청을 거절하였습니다.".formatted(friendMember.getNickname())))
                .andExpect(jsonPath("$.data.friendId").value(friend.getId()))
                .andExpect(jsonPath("$.data.friendNickname").value(friendMember.getNickname()))
                .andExpect(jsonPath("$.data.friendBio").value(friendMemberInfo.getBio()))
                .andExpect(jsonPath("$.data.friendProfileImageUrl").value(friendMemberInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("친구 거절 - 친구 요청이 없는 경우 예외 처리")
    @WithUserDetails(value = "lyh3@test.com")
    void trj2() throws Exception {
        performErrPatchFriend(
                100L,
                "reject",
                "rejectFriend",
                404,
                "친구 요청이 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("친구 수락 - 친구 엔티티는 있으나 로그인 회원과 관련 없을 경우 예외 처리")
    @WithUserDetails(value = "chs4s@test.com")
    void trj3() throws Exception {
        performErrPatchFriend(
                1L,
                "reject",
                "rejectFriend",
                400,
                "로그인한 회원과 관련된 친구가 아닙니다."
        );
    }

    @Test
    @DisplayName("친구 거절 - 받는이가 아닌 요청자가 친구 요청을 거절하는 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void trj4() throws Exception {
        performErrPatchFriend(
                1L,
                "reject",
                "rejectFriend",
                400,
                "요청한 사람이 친구 요청을 거절할 수 없습니다. 친구의 요청 수락/거절을 기다리세요."
        );
    }

    @Test
    @DisplayName("친구 거절 - 이미 친구인 경우 예외 처리")
    @WithUserDetails(value = "cjw5@test.com")
    void trj5() throws Exception {
        performErrPatchFriend(
                2L,
                "reject",
                "rejectFriend",
                400,
                "이미 친구입니다. 친구 삭제를 이용해 주세요."
        );
    }

    @Test
    @DisplayName("친구 삭제")
    @WithUserDetails(value = "hgd222@test.com")
    void td1() throws Exception {
        // 친구 엔티티
        Long friendId = 2L;
        Friend friend = friendRepository.findById(friendId).orElseThrow();

        // 로그인 회원
        Member me = memberRepository.findById(1L).orElseThrow();

        // 삭제할 친구
        Member friendMember = friend.getOther(me);
        MemberInfo friendMemberInfo = friendMember.getMemberInfo();

        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/members/me/friends/" + friendId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendController.class))
                .andExpect(handler().methodName("deleteFriend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s님이 친구 목록에서 삭제되었습니다.".formatted(friendMember.getNickname())))
                .andExpect(jsonPath("$.data.friendNickname").value(friendMember.getNickname()))
                .andExpect(jsonPath("$.data.friendBio").value(friendMemberInfo.getBio()))
                .andExpect(jsonPath("$.data.friendProfileImageUrl").value(friendMemberInfo.getProfileImageUrl()));
    }

    @Test
    @DisplayName("친구 삭제 - 친구 요청이 없는 경우")
    @WithUserDetails(value = "hgd222@test.com")
    void td2() throws Exception {
        performErrDelFriend(
                100L,
                404,
                "친구 요청이 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("친구 삭제 - 친구 요청이 수락되지 않은 경우 예외 처리")
    @WithUserDetails(value = "hgd222@test.com")
    void td3() throws Exception {
        performErrDelFriend(
                1L,
                400,
                "친구 요청이 수락되지 않았습니다."
        );
    }
}