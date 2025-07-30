package com.back.domain.member.friend.dto;

import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record FriendDto(
        @Schema(description = "친구 관계 ID")
        Long friendShipId,
        @Schema(description = "친구 ID")
        Long friendId,
        @Schema(description = "친구 닉네임")
        String friendNickname,
        @Schema(description = "친구 자기소개")
        String friendBio,
        @Schema(description = "친구 프로필 이미지 URL")
        String friendProfileImageUrl,
        @Schema(description = "친구 관계 상태")
        FriendStatus status
) {
    public FriendDto(Friend friend, Member responder, MemberInfo responderInfo) {
        this(
            friend.getId(),
            responder.getId(),
            responder.getNickname(),
            responderInfo != null ? responderInfo.getBio() : null,
            responderInfo != null ? responderInfo.getProfileImageUrl() : null,
            friend.getStatus()
        );
    }
}
