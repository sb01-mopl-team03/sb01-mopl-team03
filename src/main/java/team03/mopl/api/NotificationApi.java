package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Notification API", description = "알림 기능 관련 API")
@RequestMapping("/api/notifications")
public interface NotificationApi {

  @Operation(summary = "SSE 알림 구독", description = "SSE로 알림 실시간 구독을 시작합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "SSE 연결 성공", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  SseEmitter subscribe(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
      HttpServletResponse response
  );

  @Operation(summary = "알림 내역 조회", description = "로그인한 사용자의 알림 내역(페이지네이션 지원)을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class))),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @GetMapping
  ResponseEntity<CursorPageResponseDto<NotificationDto>> getNotifications(
      @Parameter(description = "알림 페이지네이션 파라미터") @ModelAttribute NotificationPagingDto notificationPagingDto,
      @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(summary = "단일 알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "알림 없음")
  })
  @PostMapping("/{notificationId}")
  ResponseEntity<Void> readNotification(
      @Parameter(description = "알림 ID") @PathVariable UUID notificationId,
      @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(summary = "전체 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @PostMapping("/")
  ResponseEntity<Void> readAllNotification(
      @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(summary = "단일 알림 삭제", description = "특정 알림을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "알림 없음")
  })
  @DeleteMapping("/{notificationId}")
  ResponseEntity<Void> deleteNotification(
      @Parameter(description = "알림 ID") @PathVariable UUID notificationId,
      @AuthenticationPrincipal CustomUserDetails user
  );

  @Operation(summary = "내 모든 알림 삭제", description = "로그인한 사용자의 모든 알림을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "전체 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @DeleteMapping
  ResponseEntity<Void> deleteNotificationByUserId(
      @AuthenticationPrincipal CustomUserDetails user
  );
}
