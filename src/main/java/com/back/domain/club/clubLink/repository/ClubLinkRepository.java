package com.back.domain.club.clubLink.repository;

import com.back.domain.club.clubLink.entity.ClubLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubLinkRepository extends CrudRepository<ClubLink, Integer> {
}
