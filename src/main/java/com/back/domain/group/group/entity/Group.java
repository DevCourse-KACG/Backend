package com.back.domain.group.group.entity;

import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Description("그룹 이름")
  @Column(length = 50, nullable = false)
  private String name;

  @Description("그룹 소개 글")
  @Column(columnDefinition = "TEXT")
  private String bio;

  @Description("그룹 카테고리")
  @Column(length = 50, nullable = false)
  private String category;

  @Description("주 모임 장소")
  @Column(length = 256, nullable = false)
  private String mainSpot;

  @Description("최대 인원")
  @Column(nullable = false)
  private int maximumCapacity;

  @Description("인원 모집 여부")
  @Column(nullable = false)
  private boolean recruitingStatus;

  @Description("모집 유형")
  @Column(length = 20, nullable = false)
  private String eventType;

  @Description("시작 날짜")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDateTime startDate;

  @Description("종료 날짜")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDateTime endDate;

  @Description("그룹 이미지 URL")
  @Column(length = 256)
  private String images;

  @Description("그룹 공개 여부")
  @Column(nullable = false)
  private boolean isPublic;

  @Description("그룹장 아이디")
  private Long leaderId;

  @Description("활성화 상태")
  @Column(nullable = false)
  private boolean stats = true;
}
