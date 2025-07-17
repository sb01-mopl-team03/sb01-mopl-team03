package team03.mopl.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "알림 페이지네이션 요청 DTO")
public class NotificationPagingDto {

  @Schema(description = "커서 (Base64 인코딩 문자열)", example = "YWJjZGVmZzEyMw==")
  @Pattern(regexp = "^[A-Za-z0-9-_]+={0,2}$", message = "cursor는 Base64 형식 문자열이여야 합니다.")
  private String cursor;

  @Schema(description = "한 번에 조회할 알림 개수 (최대 50)", example = "20")
  private int size = 20;
}
