package com.back.domain.member.friend.repository;

import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {


    @Query("""
            SELECT f FROM Friend f 
            WHERE (f.member1 = :requester AND f.member2 = :responder) 
            OR (f.member1 = :responder AND f.member2 = :requester)
            """)
    Optional<Friend> findByMembers(
            @Param("requester") Member requester,
            @Param("responder") Member responder
    );
}
