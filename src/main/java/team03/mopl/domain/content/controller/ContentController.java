package team03.mopl.domain.content.controller;


import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.api.ContentApi;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.content.service.ContentService;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApi {

  private final ContentService contentService;
  private final ReviewService reviewService;

  @Override
  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> getContent(@PathVariable("contentId") UUID id) {
    return ResponseEntity.ok(contentService.getContent(id));
  }

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ContentDto>> getAll(
      @Valid @ParameterObject @ModelAttribute ContentSearchRequest contentSearchRequest
  ) {
    return ResponseEntity.ok(contentService.getCursorPage(contentSearchRequest));
  }

  @Override
  @GetMapping("/{contentId}/reviews")
  public ResponseEntity<List<ReviewDto>> getAllByContent(@PathVariable UUID contentId) {
    return ResponseEntity.ok(reviewService.getAllByContent(contentId));
  }
}
