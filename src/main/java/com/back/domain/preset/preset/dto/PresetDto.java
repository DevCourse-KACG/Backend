package com.back.domain.preset.preset.dto;

import com.back.domain.member.member.entity.Member;
import com.back.domain.preset.preset.entity.Preset;

import java.util.List;

public record PresetDto(
    Long id,
    String name,
    Member owner, //Todo UserRole로 변경 예정
    List<PresetItemDto> presetItems
) {
  public PresetDto(Preset preset) {
    this(
        preset.getId(),
        preset.getName(),
        preset.getOwner(),
        preset.getPresetItems().stream()
            .map(PresetItemDto::new)
            .toList()
    );
  }
}
