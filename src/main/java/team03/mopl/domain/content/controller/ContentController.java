package team03.mopl.domain.content.controller;


import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.content.service.ContentService;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.service.ReviewService;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

  private final ContentService contentService;
  private final ReviewService reviewService;

//  @GetMapping
//  public ResponseEntity<List<ContentDto>> getAll() {
//    return ResponseEntity.ok(contentService.getAll());
//  }

  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> getContent(@PathVariable("contentId") UUID id) {
    return ResponseEntity.ok(contentService.getContent(id));
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ContentDto>> getAll(
      @Valid @ModelAttribute ContentSearchRequest contentSearchRequest
  ) {
    return ResponseEntity.ok(contentService.getCursorPage(contentSearchRequest));
  }

  @GetMapping("/{contentId}/reviews")
  public ResponseEntity<List<ReviewResponse>> getAllByContent(@PathVariable UUID contentId) {
    return ResponseEntity.ok(reviewService.getAllByContent(contentId));
  }
}
