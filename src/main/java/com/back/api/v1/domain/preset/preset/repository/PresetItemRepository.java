package com.back.api.v1.domain.preset.preset.repository;

import com.back.api.v1.domain.preset.preset.entity.PresetItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresetItemRepository extends JpaRepository<PresetItem, Long> {

}
