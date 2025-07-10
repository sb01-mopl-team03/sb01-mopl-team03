package team03.mopl.domain.dm.dto;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import team03.mopl.common.exception.dm.DmContentTooLongException;

@Data
@NoArgsConstructor
public class SendDmDto {
  private UUID senderId;
  private UUID roomId;
  private String content;

  public SendDmDto(UUID senderId, UUID roomId, String content) {
    if( content.length() > 255 ) {
      throw new DmContentTooLongException();
    }
    this.senderId = senderId;
    this.roomId = roomId;
    this.content = content;
  }
}
