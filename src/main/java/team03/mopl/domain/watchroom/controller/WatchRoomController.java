package team03.mopl.domain.watchroom.controller;


import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.api.WatchRoomApi;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchDto;
import team03.mopl.domain.watchroom.service.WatchRoomService;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class WatchRoomController implements WatchRoomApi {

  private final WatchRoomService watchRoomService;

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<WatchRoomDto>> getAllWatchRooms(
      @ParameterObject @ModelAttribute WatchRoomSearchDto request){
    return ResponseEntity.ok(watchRoomService.getAll(request));
  }

  @Override
  @GetMapping("/{roomId}")
  public ResponseEntity<WatchRoomDto> getChatRoom(@PathVariable("roomId") String roomId) {
    return ResponseEntity.ok(watchRoomService.getById(UUID.fromString(roomId)));
  }

  @Override
  @PostMapping
  public ResponseEntity<WatchRoomDto> createChatRoom(@RequestBody WatchRoomCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(watchRoomService.create(request));
  }
}
