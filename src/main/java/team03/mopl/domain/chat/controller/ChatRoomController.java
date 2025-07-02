package team03.mopl.domain.chat.controller;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.chat.dto.ChatRoomCreateRequest;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.service.ChatRoomService;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  @GetMapping
  public ResponseEntity<List<ChatRoomDto>> getChatRooms() {
    return ResponseEntity.ok(chatRoomService.getAll());
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable String roomId) {
    return ResponseEntity.ok(chatRoomService.getById(UUID.fromString(roomId)));
  }

  @PostMapping
  public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomService.create(request));
  }
}
