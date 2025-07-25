package com.back.domain.group.groupMember.entity;

import com.back.domain.checkList.itemAssign.entity.ItemAssign;
import com.back.domain.group.group.entity.Group;
import com.back.domain.member.member.entity.Member;
import com.back.global.enums.GroupMemberRole;
import com.back.global.enums.GroupMemberState;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("멤버 정보")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Description("역할")
    @Enumerated(EnumType.STRING)
    private GroupMemberRole role;

    @Description("가입 상태")
    @Enumerated(EnumType.STRING)
    private GroupMemberState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Description("체크리스트 아이템 할당 정보")
    @OneToMany(mappedBy = "groupMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemAssign> itemAssigns;
}
