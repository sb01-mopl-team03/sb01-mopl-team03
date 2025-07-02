package team03.mopl.domain.dm.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dmRooms")
public class DmRoomController {
  private final DmRoomService dmRoomService;
  private final DmService dmService;

  @GetMapping("/{dmRoomId}") // 룸 ID를 통한 조회
  public ResponseEntity<DmRoomDto> getRoom(@PathVariable UUID dmRoomId) {
    return ResponseEntity.ok(dmRoomService.getRoom(dmRoomId));
  }
  @GetMapping("/userRoom") // UserA와 UserB가 연결된 룸 조회 / 없으면 생성
  public ResponseEntity<UUID> getOrCreateRoom(
      @RequestParam UUID userA,
      @RequestParam UUID userB
  ) {
    DmRoomDto dmRoomDto = dmRoomService.findOrCreateRoom(userA, userB);
    return ResponseEntity.ok(dmRoomDto.getId());
  }
  @GetMapping("/") //유저의 모든 룸 조회
  public ResponseEntity<List<DmRoomDto>> getAllRooms(@RequestParam UUID userId) {
    return ResponseEntity.ok().body(dmRoomService.getAllRoomsForUser(userId));
  }
  @DeleteMapping("/{roomId}") // 유저가 속한 룸 삭제
  public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId, @RequestParam UUID userId) {
    dmRoomService.deleteRoom(userId, roomId);
    return ResponseEntity.noContent().build();
  }



}
