package team03.mopl.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;

@Service
@RequiredArgsConstructor
public class SseEmitterManager {
  //추후 에러 로깅 필요
  private final Map<UUID, SseEmitter> userConnections = new ConcurrentHashMap<>();

  /*
  * 클라이언트 쪽에서 N분마다 SSE연결을 재연결해줘야 한다.
  * */
  public SseEmitter subscribe(UUID userId) {
    //기존에 연결된 sse가 있다면 연결 종료
    SseEmitter sseEmitter = userConnections.get(userId);
    if (sseEmitter != null) {
      sseEmitter.complete();
    }
    SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); //10분?
    userConnections.put(userId, emitter);
    //오류 발생 시 제거
    emitter.onCompletion(() -> userConnections.remove(userId));
    emitter.onTimeout(() -> userConnections.remove(userId));
    emitter.onError((e) -> userConnections.remove(userId));

    try {
      // 연결 성공 알림
      emitter.send(SseEmitter.event()
          .name("connected")
          .data("알림 서비스에 연결되었습니다."));

    } catch (IOException e) {
      // 전송 중 예외 발생시 연결 제거 및 종료 처리
      userConnections.remove(userId);
      emitter.completeWithError(e);
    }
    return emitter;
  }

  // 특정 사용자에게 알림 전송
  public void sendNotification(UUID userId, Notification notification) {
    SseEmitter emitter = userConnections.get(userId); // 사용자 연결 확인

    if (emitter != null) {
      try {
        String eventName = notification.getType().getEventName();
        emitter.send(SseEmitter.event()
            .name(eventName) // 이벤트 이름
            .id(String.valueOf(notification.getId())) // 고유 이벤트 ID ( String만 허용 )
            .data(NotificationDto.from(notification))); // 알림 데이터 전송

        System.out.println("알림 전송 성공: " + userId + " - " + notification.getContent()); //getType() 가 나을수도?

      } catch (IOException e) {
        System.out.println("알림 전송 실패: " + userId);
        userConnections.remove(userId);
      }
    } else {
      // 연결 상태가 아닐 경우
      System.out.println("사용자 " + userId + "가 연결되어 있지 않음");
    }
  }

  // 현재 온라인 사용자 수
  public int getOnlineUserCount() {
    return userConnections.size();
  }

  // 특정 사용자 온라인 여부
  public boolean isUserOnline(UUID userId) {
    return userConnections.containsKey(userId);
  }
}
