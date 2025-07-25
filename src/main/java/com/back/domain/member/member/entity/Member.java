package com.back.domain.member.member.entity;


import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.friend.entity.Friend;
import com.back.domain.preset.preset.entity.Preset;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.PRIVATE)
  @EqualsAndHashCode.Include
  private Long id;

  @Description("닉네임")
  @Column(length = 50, nullable = false)
  private String nickname;

  @Description("비밀번호")
  private String password;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "member")
  private MemberInfo memberInfo; // 상세 정보 (회원 전용)

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "owner")
  private List<Preset> presets; // 프리셋 목록 (회원 전용)

  // 친구 관계 (내가 포함된 모든 관계)
  @OneToMany(mappedBy = "member1", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Friend> friendshipsAsMember1;
  @OneToMany(mappedBy = "member2", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Friend> friendshipsAsMember2;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClubMember> clubMembers; // 소속 그룹 목록

}
