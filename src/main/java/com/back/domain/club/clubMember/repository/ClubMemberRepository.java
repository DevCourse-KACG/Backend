package com.back.domain.club.clubMember.repository;

import com.back.domain.club.clubMember.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

}
