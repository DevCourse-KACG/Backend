package com.back.api.v1.domain.checkList.checkList.entity;

import com.back.api.v1.domain.schedule.schedule.entity.Schedule;
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
public class CheckList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("활성화 여부")
    private boolean isActive;

    @Description("연동된 일정")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Schedule schedule;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "checkList")
    @Description("체크리스트 아이템들")
    private List<CheckListItem> checkListItems;
}
