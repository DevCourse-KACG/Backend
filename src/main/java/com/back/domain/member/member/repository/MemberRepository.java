package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNickname(String nickname);

    boolean existsByNicknameAndTag(String nickname, String tag);

    Optional<Member> findByNicknameAndTag(String nickname, String tag);

    // 회원 ID로 회원 정보를 조회하며, 회원 상세 정보 포함 (n+1 쿼리 방지)
    /*@EntityGraph(attributePaths = "memberInfo")
    Optional<Member> findByIdWithMemberInfo(Long memberId);*/

    // 회원 ID로 회원 정보를 조회하며, 친구 관계를 포함 (n+1 쿼리 방지)
    @EntityGraph(attributePaths = {
            "friendshipsAsMember1.member2.memberInfo", // member1로 등록 된 경우 친구 member2의 정보
            "friendshipsAsMember2.member1.memberInfo"  // member2로 등록 된 경우 친구 member1의 정보
    })
    Optional<Member> findWithFriendsById(Long memberId);

    @Query("""
    select case when count(m) > 0 then true else false end
    from Member m
    join m.clubMembers cm
    where m.nickname = :nickname
      and m.memberType = 'GUEST'
      and cm.club.id = :clubId
""")
    boolean existsGuestNicknameInClub(String nickname, @Param("clubId") Long clubId);

    @Query("""
    select m
    from Member m
    join m.clubMembers cm
    where m.nickname = :nickname
      and m.memberType = 'GUEST'
      and cm.club.id = :clubId
""")
    Optional<Member> findByGuestNicknameInClub(String nickname, @Param("clubId") Long clubId);
}
