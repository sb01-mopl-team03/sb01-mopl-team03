package team03.mopl.domain.watchroom.controller;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.service.WatchRoomService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class WatchRoomController {

  private final WatchRoomService watchRoomService;

  @GetMapping
  public ResponseEntity<List<WatchRoomDto>> getChatRooms() {
    return ResponseEntity.ok(watchRoomService.getAll());
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<WatchRoomDto> getChatRoom(@PathVariable("roomId") String roomId) {
    return ResponseEntity.ok(watchRoomService.getById(UUID.fromString(roomId)));
  }

  @PostMapping
  public ResponseEntity<WatchRoomDto> createChatRoom(@RequestBody WatchRoomCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(watchRoomService.create(request));
  }
}
