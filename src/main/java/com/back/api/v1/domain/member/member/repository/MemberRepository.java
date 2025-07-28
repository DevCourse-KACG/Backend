package com.back.api.v1.domain.member.member.repository;

import com.back.api.v1.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String duplicateNickname);
    Optional<Member> findByNickname(String nickname);
}
