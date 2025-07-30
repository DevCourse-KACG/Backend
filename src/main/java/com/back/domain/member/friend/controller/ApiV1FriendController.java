package com.back.domain.member.friend.controller;

import com.back.domain.member.friend.dto.FriendAddReqBody;
import com.back.domain.member.friend.dto.FriendDto;
import com.back.domain.member.friend.service.FriendService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/friends")
@RequiredArgsConstructor
@Tag(name = "ApiV1FriendController", description = "친구 컨트롤러")
public class ApiV1FriendController {
    private final FriendService friendService;

    @PostMapping
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

}
