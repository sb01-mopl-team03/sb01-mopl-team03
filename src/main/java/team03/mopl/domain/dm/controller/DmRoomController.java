package team03.mopl.domain.dm.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.api.DmRoomApi;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dmRooms")
@Slf4j
public class DmRoomController implements DmRoomApi {
  private final DmRoomService dmRoomService;

  @Override
  @GetMapping("/{dmRoomId}") // 룸 ID를 통한 조회
  public ResponseEntity<DmRoomDto> getRoom(@PathVariable(name = "dmRoomId") UUID dmRoomId) {
    return ResponseEntity.ok(dmRoomService.getRoom(dmRoomId));
  }

  @Override
  @GetMapping("/userRoom") // UserA와 UserB가 연결된 룸 조회 / 없으면 생성
  public ResponseEntity<UUID> getOrCreateRoom(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "userB") UUID userB
  ) {
    UUID userA = userDetails.getId();
    DmRoomDto dmRoomDto = dmRoomService.findOrCreateRoom(userA, userB);
    return ResponseEntity.ok(dmRoomDto.getId());
  }

  @Override
  @GetMapping("/") //유저의 모든 룸 조회
  public ResponseEntity<List<DmRoomDto>> getAllRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
    UUID userId = userDetails.getId();
    return ResponseEntity.ok().body(dmRoomService.getAllRoomsForUser(userId));
  }

  @Override
  @DeleteMapping("/{roomId}") // 유저가 속한 룸 삭제
  public ResponseEntity<Void> deleteRoom(
      @PathVariable(name = "roomId") UUID roomId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    dmRoomService.deleteRoom(userId, roomId);
    return ResponseEntity.noContent().build();
  }
}
