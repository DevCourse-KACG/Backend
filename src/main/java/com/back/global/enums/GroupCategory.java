package com.back.global.enums;

public enum GroupCategory {
    STUDY("스터디"),
    HOBBY("취미"),
    SPORTS("운동"),
    TRAVEL("여행"),
    CULTURE("문화"),
    FOOD("음식"),
    PARTY("파티"),
    OTHER("기타");

    private final String description;

    GroupCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
