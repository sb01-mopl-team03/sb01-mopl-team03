package team03.mopl.domain.dm.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmRequest {
  private UUID senderId;
  private UUID roomId;
  private String content;
  private boolean isRead;
  private LocalDateTime createdAt;
}
