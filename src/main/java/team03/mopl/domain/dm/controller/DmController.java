package team03.mopl.domain.dm.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmPagingDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dm")
@Slf4j
public class DmController {
  private final DmService dmService;

  @GetMapping("/{roomId}")
  public ResponseEntity<CursorPageResponseDto<DmDto>> getDm(
      @PathVariable("roomId") UUID roomId,
      @Valid @ModelAttribute DmPagingDto dmPagingDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    dmService.readAll(roomId, userDetails.getId()); //dm 리스트를 가져온다는 건 모두 읽겠다는 뜻
    return ResponseEntity.ok(dmService.getDmList(roomId, dmPagingDto, userDetails.getId()));
  }
}
