package team03.mopl.domain.dm.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dm")
public class DmController {
  private final DmService dmService;
  private final DmRoomService dmRoomService;

  // 유저가 룸안의 dm 메시지 가져오기 ( 커서 페이징 필요? )
  @GetMapping("/{roomId}/dm")
  public ResponseEntity<List<DmDto>> getDm(@PathVariable("roomId") UUID roomId, @RequestParam("userId") UUID userId) {
    return ResponseEntity.ok(dmService.getDmList(roomId, userId));
  }
}
