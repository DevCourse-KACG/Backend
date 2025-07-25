package com.back.domain.member.member.entity;


import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Description("닉네임")
  @Column(length = 50, nullable = false)
  private String nickname;

  @Description("이메일")
  @Column(length = 100, nullable = false, unique = true)
  private String email;

  @Description("자기소개")
  @Column(columnDefinition = "TEXT")
  private String bio;

  @Description("프로필 이미지 URL")
  @Column(length = 256)
  private String images;

  @Description("비밀번호")
  private String password;
}
