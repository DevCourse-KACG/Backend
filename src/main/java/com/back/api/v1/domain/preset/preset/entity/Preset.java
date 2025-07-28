package com.back.api.v1.domain.preset.preset.entity;

import com.back.api.v1.domain.member.member.entity.Member;
import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Preset {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Description("프리셋 이름")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member owner;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "preset")
    private List<PresetItem> presetItems;
}
