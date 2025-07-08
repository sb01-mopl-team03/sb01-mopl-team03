package team03.mopl.domain.dm.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dmRooms")
@Slf4j
public class DmRoomController {
  private final DmRoomService dmRoomService;

  @GetMapping("/{dmRoomId}") // 룸 ID를 통한 조회
  public ResponseEntity<DmRoomDto> getRoom(@PathVariable(name = "dmRoomId") UUID dmRoomId) {
    log.info("getRoom - DM 룸 단건 조회 요청: roomId={}", dmRoomId);
    return ResponseEntity.ok(dmRoomService.getRoom(dmRoomId));
  }
  @GetMapping("/userRoom") // UserA와 UserB가 연결된 룸 조회 / 없으면 생성
  public ResponseEntity<UUID> getOrCreateRoom(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "userB") UUID userB
  ) {
    UUID userA = userDetails.getId();
    log.info("getOrCreateRoom - DM 룸 조회/생성 요청: userA={}, userB={}", userA, userB);
    DmRoomDto dmRoomDto = dmRoomService.findOrCreateRoom(userA, userB);
    return ResponseEntity.ok(dmRoomDto.getId());
  }
  @GetMapping("/") //유저의 모든 룸 조회
  public ResponseEntity<List<DmRoomDto>> getAllRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
    UUID userId = userDetails.getId();
    log.info("getAllRooms - 유저의 모든 DM 룸 조회 요청: userId={}", userId);
    return ResponseEntity.ok().body(dmRoomService.getAllRoomsForUser(userId));
  }
  @DeleteMapping("/{roomId}") // 유저가 속한 룸 삭제
  public ResponseEntity<Void> deleteRoom(
      @PathVariable(name = "roomId") UUID roomId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    log.info("deleteRoom - DM 룸 삭제 요청: userId={}, roomId={}", userId, roomId);
    dmRoomService.deleteRoom(userId, roomId);
    return ResponseEntity.noContent().build();
  }
}
