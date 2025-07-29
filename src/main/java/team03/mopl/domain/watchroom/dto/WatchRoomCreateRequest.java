package team03.mopl.domain.watchroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

//todo - @AuthenticationPrincipal 적용으로 수정
@Schema(description = "실시간 같이 보기 생성 요청 DTO")
public record WatchRoomCreateRequest(

    @Schema(description = "컨텐츠 ID", example = "c1d2e3f4-5678-1234-9012-fedcbafedcba")
    UUID contentId,

    @Schema(description = "실시간 같이 보기(Owner) ID", example = "a1b2c3d4-1234-5678-9012-abcdefabcdef")
    UUID ownerId,

    @Schema(description = "시청방 제목", example = "케데헌 같이 볼 사람!")
    String title
){
}
