package com.back.api.v1.domain.club.clubMember.repository;

import com.back.api.v1.domain.club.clubMember.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

}
