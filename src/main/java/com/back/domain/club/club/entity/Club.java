package com.back.domain.club.club.entity;

import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.schedule.schedule.entity.Schedule;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.EventType;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Club {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.PRIVATE)
  @EqualsAndHashCode.Include
  private Long id;

  @Description("그룹 이름")
  @Column(length = 50, nullable = false)
  private String name;

  @Description("그룹 소개 글")
  @Column(columnDefinition = "TEXT")
  private String bio;

  @Description("그룹 카테고리")
  @Column(length = 50, nullable = false)
  @Enumerated(EnumType.STRING)
  private ClubCategory category;

  @Description("주 모임 장소")
  @Column(length = 256, nullable = false)
  private String mainSpot; // TODO : 지도 연동하면 좌표로 바꿔야 됨

  @Description("최대 인원")
  @Column(nullable = false)
  private int maximumCapacity;

  @Description("인원 모집 여부")
  @Column(nullable = false)
  private boolean recruitingStatus = true;

  @Description("모집 유형")
  @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
  private EventType eventType;

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

  @Description("구성원")
  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ClubMember> clubMembers = new ArrayList<>();

  @Description("일정 목록")
  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Schedule> clubSchedules = new ArrayList<>();
}
