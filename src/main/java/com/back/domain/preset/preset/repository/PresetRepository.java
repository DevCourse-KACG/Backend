package com.back.domain.preset.preset.repository;

import com.back.domain.preset.preset.entity.Preset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresetRepository extends JpaRepository<Preset, Long> {

    // Preset 엔티티에 대한 CRUD 메소드가 JpaRepository에 정의되어 있음
    // 추가적인 메소드가 필요하다면 여기에 정의할 수 있음
}
