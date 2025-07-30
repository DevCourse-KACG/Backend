package com.back.domain.member.friend.controller;

import com.back.domain.member.friend.dto.FriendAddReqBody;
import com.back.domain.member.friend.dto.FriendDelDto;
import com.back.domain.member.friend.dto.FriendDto;
import com.back.domain.member.friend.service.FriendService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members/me/friends")
@RequiredArgsConstructor
@Tag(name = "ApiV1FriendController", description = "친구 컨트롤러")
public class ApiV1FriendController {
    private final FriendService friendService;

    @PostMapping
    @Operation(summary = "친구 추가")
    public RsData<FriendDto> addFriend(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody FriendAddReqBody reqBody
    ) {
        FriendDto friendDto = friendService.addFriend(user.getId(), reqBody.friend_email());

        return RsData.of(
                201,
                "%s 에게 친구 추가 요청이 성공적으로 처리되었습니다.".formatted(reqBody.friend_email()),
                friendDto
        );
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "친구 삭제")
    public RsData<FriendDelDto> deleteFriend(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long friendId
    ) {
        FriendDelDto friendDelDto = friendService.deleteFriend(user.getId(), friendId);

        return RsData.of(
                200,
                "%s님이 친구 목록에서 삭제되었습니다.".formatted(friendDelDto.friendNickname()),
                friendDelDto
        );
    }

}
