package com.back.api.v1.domain.member.friend.repository;

import com.back.api.v1.domain.member.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

}
