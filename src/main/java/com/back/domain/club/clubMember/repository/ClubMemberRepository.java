package com.back.domain.club.clubMember.repository;

import com.back.domain.club.clubMember.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findAllByClubId(Long clubId);
}
