package com.back.domain.checkList.checkList.controller;

import com.back.domain.checkList.checkList.dto.CheckListDto;
import com.back.domain.checkList.checkList.dto.CheckListWriteReqDto;
import com.back.domain.checkList.checkList.service.CheckListService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Tag(name="ApiV1CheckListController", description="체크리스트 API V1 컨트롤러")
public class ApiV1CheckListController {
  private final CheckListService checkListService;

  @PostMapping
  @Operation(summary = "체크리스트 생성")
  public ResponseEntity<RsData<CheckListDto>> write(@Valid @RequestBody CheckListWriteReqDto checkListWriteReqDto) {
    RsData<CheckListDto> checkListDto = checkListService.write(checkListWriteReqDto);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }
}
