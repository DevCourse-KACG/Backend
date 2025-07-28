package com.back.global.enums;

public enum ClubMemberRole {
    PARTICIPANT("일반 회원"),
    MANAGER("관리자"),
    HOST("소유자");

    private final String description;

    ClubMemberRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ClubMemberRole fromString(String role) {
        for (ClubMemberRole clubMemberRole : ClubMemberRole.values()) {
            if (clubMemberRole.name().equalsIgnoreCase(role)) {
                return clubMemberRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
