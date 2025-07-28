package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String duplicateNickname);
    Optional<Member> findByNickname(String nickname);

    boolean existsByNicknameAndTag(@NotBlank String nickname, String tag);
}
