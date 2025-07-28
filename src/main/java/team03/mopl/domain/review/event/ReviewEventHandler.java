package team03.mopl.domain.review.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import team03.mopl.domain.content.service.ContentService;

@Component
@RequiredArgsConstructor
public class ReviewEventHandler {

  private final ContentService contentService;

  @EventListener
  public void handleReviewEvent(ReviewEvent event) {
    contentService.updateContentRating(event.contentId());
  }
}
