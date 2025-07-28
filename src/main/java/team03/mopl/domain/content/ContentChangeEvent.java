package team03.mopl.domain.content;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

// Content 엔티티의 변경을 알리는 이벤트 클래스
@Getter
public class ContentChangeEvent extends ApplicationEvent {

  public enum Type {
    SAVE,   // 생성 또는 업데이트
    DELETE  // 삭제
  }

  private final Content content; // SAVE 이벤트일 때 Content 객체
  private final UUID contentId;  // DELETE 이벤트일 때 Content ID
  private final Type type;

  // SAVE 이벤트 생성자
  public ContentChangeEvent(Object source, Content content) {
    super(source);
    this.content = content;
    this.contentId = content.getId();
    this.type = Type.SAVE;
  }

  // DELETE 이벤트 생성자
  public ContentChangeEvent(Object source, UUID contentId) {
    super(source);
    this.content = null; // 삭제 시에는 Content 객체가 없을 수 있음
    this.contentId = contentId;
    this.type = Type.DELETE;
  }
}
