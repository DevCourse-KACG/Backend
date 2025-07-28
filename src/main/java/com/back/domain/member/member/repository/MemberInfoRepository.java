package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.MemberInfo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findByEmail(@NotBlank String email);
}
