package team03.mopl.domain.dm.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dm")
@Slf4j
public class DmController {
  private final DmService dmService;

  // 유저가 룸안의 dm 메시지 가져오기 ( 커서 페이징 필요? )
  @GetMapping("/{roomId}")
  public ResponseEntity<List<DmDto>> getDm(@PathVariable("roomId") UUID roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    System.out.println("DmController.getDm");
    return ResponseEntity.ok(dmService.getDmList(roomId, userDetails.getId()));
  }
}
