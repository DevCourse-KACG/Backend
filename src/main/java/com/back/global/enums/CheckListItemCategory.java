package com.back.global.enums;

public enum CheckListItemCategory {
    // 준비물
    // 예약
    // 사전 작업
    // 기타
    PREPARATION("준비물"),
    RESERVATION("예약"),
    PRE_WORK("사전 작업"),
    ETC("기타");

    private final String description;

    CheckListItemCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
