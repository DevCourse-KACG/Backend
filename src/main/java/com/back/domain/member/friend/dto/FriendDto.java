package com.back.domain.member.friend.dto;

import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;

public record FriendDto(
    Long friendShipId,
    Long friendId,
    String friendNickname,
    String friendBio,
    String friendProfileImageUrl,
    FriendStatus status
) {
    public FriendDto(Friend friend, Member responder, MemberInfo responderInfo) {
        this(
            friend.getId(),
            responder.getId(),
            responder.getNickname(),
            responderInfo.getBio(),
            responderInfo.getProfileImageUrl(),
            friend.getStatus()
        );
    }
}
