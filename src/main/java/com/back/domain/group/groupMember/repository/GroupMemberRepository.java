package com.back.domain.group.groupMember.repository;

import com.back.domain.group.groupMember.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

}
