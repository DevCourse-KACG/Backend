package com.back.domain.schedule.schedule.entity;

import com.back.domain.group.group.entity.Group;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private Long id;

    @Description("일정 제목")
    private String title;

    @Description("일정 내용")
    private String content;

    @Description("일정 시작 날짜")
    private LocalDateTime startDate;

    @Description("일정 종료 날짜")
    private LocalDateTime endDate;

    @Description("일정 장소")
    private String spot; //TODO : 나중에 지도 연동하면 좌표로 변경

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Group group; // 그룹 일정


}
