package com.back.domain.club.clubLink.entity;

import com.back.domain.club.club.entity.Club;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClubLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("초대 코드")
    private String inviteCode;

    @Description("링크 생성 날짜")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDate createAt;

    @Description("링크 만료 날짜")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDate expiresAt;

    @Description("클럽 정보")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    @Setter
    private Club club;


    //===================================빌더=========================================
    @Builder
    public ClubLink(String inviteCode, LocalDate createAt, LocalDate expiresAt, Club club) {
        this.inviteCode = inviteCode;
        this.createAt = createAt;
        this.expiresAt = expiresAt;
        this.club = club;
    }
}
