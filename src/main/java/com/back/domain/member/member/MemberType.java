package com.back.domain.member.member;

public enum MemberType {
    MEMBER("회원"),
    GUEST("비회원");

    private final String description;

    MemberType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
