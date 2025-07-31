package com.back.domain.club.clubMember.repository;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.ClubMemberState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findAllByClubId(Long clubId);

    @Query("""
    SELECT cm.member.memberInfo.email
    FROM ClubMember cm
    WHERE cm.club.id = :clubId
      AND cm.member.memberInfo.email IN :emails
""")
    List<String> findExistingEmails(@Param("clubId") Long clubId,
                                    @Param("emails") List<String> emails);

    Optional<ClubMember> findByClubAndMember(Club club, Member member);

    List<ClubMember> findByClubAndState(Club club, ClubMemberState clubMemberState);

    List<ClubMember> findByClub(Club club);

    boolean existsByClubAndMember(Club club, Member member);
}
