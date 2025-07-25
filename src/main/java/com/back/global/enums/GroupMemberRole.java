package com.back.global.enums;

public enum GroupMemberRole {
    PARTICIPANT("일반 회원"),
    MANAGER("관리자"),
    HOST("소유자");

    private final String description;

    GroupMemberRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
