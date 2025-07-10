package team03.mopl.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.repository.NotificationRepository;
import team03.mopl.domain.notification.repository.NotificationRepositoryCustom;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{
  private final NotificationRepository notificationRepository;
  private final NotificationRepositoryCustom notificationRepositoryCustom;
  private final EmitterService emitterService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public UUID sendNotification(NotificationDto notificationDto) {
    log.info("sendNotification - 알림 전송 요청: receiverId={}, type={}, content={}",
        notificationDto.getReceiverId(),
        notificationDto.getNotificationType(),
        notificationDto.getContent());
    Notification notification = new Notification(notificationDto.getReceiverId(), notificationDto.getNotificationType(), notificationDto.getContent());
    Notification saved = notificationRepository.save(notification);
    emitterService.sendNotificationToMember(notificationDto.getReceiverId(), saved );
    //sseEmitterManager.sendNotification(receiverId, save);
    log.info("알림 전송 완료: notificationId={}, receiverId={}", saved.getId(), saved.getReceiverId());
    return notification.getId();
  }

  @Override
  public CursorPageResponseDto<NotificationDto> getNotifications(NotificationPagingDto notificationPagingDto, UUID receiverId) {
    log.info("getNotifications - 알림 내역 조회: receiverId={}", receiverId);
    String cursor = notificationPagingDto.getCursor();
    Cursor decodeCursor;
    String mainCursorValue = null;
    String subCursorValue = null;
    if (cursor != null && !cursor.isEmpty()) {
      decodeCursor = decodeCursor(cursor);
      mainCursorValue = decodeCursor.lastValue();
      subCursorValue = decodeCursor.lastId();
    }
    int size = notificationPagingDto.getSize();
    long totalElements = notificationRepository.count();
    List<Notification> list = notificationRepositoryCustom.findByCursor(receiverId, size+1, mainCursorValue, subCursorValue);
    boolean hasNext = list.size() > size;

    //21개
    List<NotificationDto> dtoList = list.stream().map(NotificationDto::from).toList();

    String nextCursor = null;
    if (hasNext) {
      //더 보낼게 있는 것들
      nextCursor = dtoList.get(dtoList.size() - 1).getCreatedAt().toString();
      dtoList = dtoList.subList(0, size); //20개가 넘치니 자름
    }
    log.info("getNotifications - 알림 내역 조회 완료: receiverId={}, 알림 수={}", receiverId, dtoList.size());
    return CursorPageResponseDto.<NotificationDto>builder()
        .data(dtoList)
        .nextCursor(nextCursor)
        .size(dtoList.size())
        .totalElements(totalElements)
        .hasNext(hasNext).build();
  }

  private Cursor decodeCursor(String base64) {
    try {
      String json = new String(Base64.getDecoder().decode(base64));
      return objectMapper.readValue(json, Cursor.class);
    } catch (Exception e) {
      log.warn("Base64 문자열을 디코딩하여 객체로 변환 중 오류 발생", e);
      throw new IllegalArgumentException("잘못된 커서 형식입니다.");
    }
  }

  @Override
  public void markAllAsRead(UUID receiverId) {
    log.info("markAllAsRead - 알림 읽음 처리 시작: receiverId={}", receiverId);

    List<Notification> unread = notificationRepository
        .findByReceiverIdAndIsRead(receiverId, false);

    unread.forEach(Notification::setIsRead);
    notificationRepository.saveAll(unread);
    //읽었다고 판단한 알림들은 재전송용 캐시에서 삭제
    emitterService.deleteNotificationCaches(unread);
    log.info("markAllAsRead - 알림 읽음 처리 완료: receiverId={}, 읽은 알림 수={}", receiverId, unread.size());
  }

}
