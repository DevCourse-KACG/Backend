package com.back.domain.checkList.checkList.controller;

import com.back.domain.checkList.checkList.dto.CheckListDto;
import com.back.domain.checkList.checkList.dto.CheckListUpdateReqDto;
import com.back.domain.checkList.checkList.dto.CheckListWriteReqDto;
import com.back.domain.checkList.checkList.service.CheckListService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/{checkListId}")
  @Operation(summary = "체크리스트 조회")
  public ResponseEntity<RsData<CheckListDto>> getCheckList(@PathVariable Long checkListId) {
    RsData<CheckListDto> checkListDto = checkListService.getCheckList(checkListId);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }

  @PutMapping("/{checkListId}")
  @Operation(summary = "체크리스트 수정")
  public ResponseEntity<RsData<CheckListDto>> updateCheckList(@PathVariable Long checkListId, @Valid @RequestBody CheckListUpdateReqDto checkListUpdateReqDto) {
    RsData<CheckListDto> checkListDto = checkListService.updateCheckList(checkListId, checkListUpdateReqDto);

    return ResponseEntity.status(checkListDto.code()).body(checkListDto);
  }
}
