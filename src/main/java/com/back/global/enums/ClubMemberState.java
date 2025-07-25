package com.back.global.enums;

public enum ClubMemberState {
    INVITED("초대됨"),
    JOINING("가입 중"),
    APPLYING("가입 신청"),
    WITHDRAWN("탈퇴");

    private final String description;
    ClubMemberState(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
