package com.back.domain.member.friend.dto;

import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record FriendsResDto(
        @Schema(description = "친구 관계 ID")
        Long friendId,
        @Schema(description = "친구 회원 ID")
        Long friendMemberId,
        @Schema(description = "친구 닉네임")
        String friendNickname,
        @Schema(description = "친구 프로필 이미지 URL")
        String friendProfileImageUrl,
        @Schema(description = "친구 관계 상태")
        FriendStatus status
) {
    public FriendsResDto(Friend friend, Member target) {
        this(
            friend.getId(),
            target.getId(),
            target.getNickname(),
            target.getMemberInfo() != null ? target.getMemberInfo().getProfileImageUrl() : null,
            friend.getStatus()
        );
    }
}
