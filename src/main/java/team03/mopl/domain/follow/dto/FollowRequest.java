package team03.mopl.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팔로우 요청 DTO")
public class FollowRequest {

  @Schema(description = "팔로우를 요청하는 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
  UUID followerId;

  @Schema(description = "팔로우 대상 사용자 ID", example = "987f6543-e21b-43cd-a987-6543210fedcb")
  UUID followingId;
}
