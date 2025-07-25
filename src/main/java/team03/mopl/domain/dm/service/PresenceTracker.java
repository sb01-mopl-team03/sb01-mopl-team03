package team03.mopl.domain.dm.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Component
@RequiredArgsConstructor
public class PresenceTracker {
  private final UserRepository userRepository;
  // userId → 접속 중인 roomId 목록
  private final ConcurrentMap<UUID, Set<UUID>> userRoomMap = new ConcurrentHashMap<>();

  // 채팅방 입장
  public void enterRoom(String username, UUID roomId) {
    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);
    userRoomMap.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet()).add(roomId);
  }

  // 채팅방 퇴장
  public void exitRoom(String username, UUID roomId) {
    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);
    Set<UUID> rooms = userRoomMap.get(user.getId());
    if (rooms != null) {
      rooms.remove(roomId);
      if (rooms.isEmpty()) {
        userRoomMap.remove(user.getId());
      }
    }
  }

  // 해당 사용자가 해당 방에 접속해 있는지?
  public boolean isInRoom(UUID userId, UUID roomId) {
    return userRoomMap.getOrDefault(userId, Collections.emptySet()).contains(roomId);
  }

  // 모든 정보 제거 (예: WebSocket 연결 끊김)
  public void clearUser(UUID userId) {
    userRoomMap.remove(userId);
  }

  // 디버깅용: 현재 접속자 전체 확인
  public Map<UUID, Set<UUID>> getAllPresence() {
    return Collections.unmodifiableMap(userRoomMap);
  }
}

