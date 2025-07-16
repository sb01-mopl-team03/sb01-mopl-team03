package team03.mopl.domain.curation.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.dto.CursorPageRequest;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class CurationController {

  private final CurationService curationService;

  @PostMapping()
  public ResponseEntity<KeywordDto> registerKeyword(@Valid @RequestBody KeywordRequest request) {
    KeywordDto keyword = curationService.registerKeyword(request.userId(), request.keyword());
    return ResponseEntity.ok(keyword);
  }

  @GetMapping("/{keywordId}/contents")
  public ResponseEntity<CursorPageResponseDto<ContentDto>> getRecommendations(
      @PathVariable UUID keywordId,
      @ModelAttribute CursorPageRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();

    CursorPageResponseDto<ContentDto> recommendations = curationService.getRecommendationsByKeyword(keywordId, userId, request);
    return ResponseEntity.ok(recommendations);
  }

  @DeleteMapping("/{keywordId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID keywordId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();

    curationService.delete(keywordId, userId);
    return ResponseEntity.noContent().build();
  }
}
