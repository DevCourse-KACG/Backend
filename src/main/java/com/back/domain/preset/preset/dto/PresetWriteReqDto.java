package com.back.domain.preset.preset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PresetWriteReqDto(
    @NotBlank
    String name,
    @Valid
    List<PresetItemWriteReqDto> presetItems
) {
}
