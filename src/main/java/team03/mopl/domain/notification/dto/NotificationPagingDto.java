package team03.mopl.domain.notification.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPagingDto {
  @Pattern(regexp = "^[A-Za-z0-9-_]+={0,2}$", message = "cursor는 Base64 형식 문자열이여야 합니다.")
  private String cursor;
  private int size = 20;
}
