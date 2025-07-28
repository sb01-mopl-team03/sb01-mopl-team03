package team03.mopl.domain.dm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DM 메시지 전송 요청 DTO")
public class DmRequest {

  @Schema(description = "보낸 사람 ID", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID senderId;

  @Schema(description = "DM 방 ID", example = "987f6543-e21b-43cd-a987-6543210fedcb")
  private UUID roomId;

  @Schema(description = "메시지 내용", example = "안녕하세요, 지금 시간 어때요?")
  private String content;
}
