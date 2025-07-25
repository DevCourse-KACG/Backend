package com.back.domain.checkList.itemAssign.entity;

import com.back.domain.checkList.checkList.entity.CheckListItem;
import com.back.domain.group.groupMember.entity.GroupMember;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

/**
 * 멤버를 checklistItem에 할당하는 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_member_id", "check_list_item_id"})
)
public class ItemAssign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    @Description("할당된 인원")
    private GroupMember groupMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_list_item_id", nullable = false)
    @Description("할당된 체크리스트 아이템")
    private CheckListItem checkListItem;

    @Description("체크 여부")
    private boolean isChecked;
}
