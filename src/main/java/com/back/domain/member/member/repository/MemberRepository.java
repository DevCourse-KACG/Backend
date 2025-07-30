package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String duplicateNickname);
    Optional<Member> findByNickname(String nickname);

    boolean existsByNicknameAndTag(@NotBlank String nickname, String tag);

    Optional<Member> findByNicknameAndTag(String nickname, String tag);

    @Query("""
    select case when count(m) > 0 then true else false end
    from Member m
    join m.clubMembers cm
    where m.nickname = :nickname
      and m.memberType = 'GUEST'
      and cm.club.id = :clubId
""")
    boolean existsGuestNicknameInClub(String nickname, @Param("clubId") Long clubId);
}
