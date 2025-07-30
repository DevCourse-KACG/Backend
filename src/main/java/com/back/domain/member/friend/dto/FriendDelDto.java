package com.back.domain.member.friend.dto;

import com.back.domain.member.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record FriendDelDto(
        @Schema(description = "친구 ID")
        Long friendId,
        @Schema(description = "친구 닉네임")
        String friendNickname,
        @Schema(description = "친구 자기소개")
        String friendBio,
        @Schema(description = "친구 프로필 이미지 URL")
        String friendProfileImageUrl
) {
    public FriendDelDto(Member friendMember) {
        this(
            friendMember.getId(),
            friendMember.getNickname(),
            friendMember.getMemberInfo() != null ? friendMember.getMemberInfo().getBio() : null,
            friendMember.getMemberInfo() != null ? friendMember.getMemberInfo().getProfileImageUrl() : null
        );
    }
}
