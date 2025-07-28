package team03.mopl.domain.curation.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.api.CurationApi;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.service.ContentSearchService;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class CurationController implements CurationApi {

  private final CurationService curationService;
  private final ContentSearchService contentSearchService;

  @Override
  @PostMapping
  public ResponseEntity<KeywordDto> registerKeyword(@Valid @RequestBody KeywordRequest request) {
    KeywordDto keyword = curationService.registerKeyword(request.userId(), request.keyword());
    return ResponseEntity.ok(keyword);
  }

  @GetMapping("/{keywordId}/contents")
  public ResponseEntity<List<ContentDto>> getRecommendations(
      @PathVariable UUID keywordId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();

    List<ContentDto> recommendations = curationService.getRecommendationsByKeyword(keywordId, userId);
    return ResponseEntity.ok(recommendations);
  }

  @Override
  @DeleteMapping("/{keywordId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID keywordId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();

    curationService.delete(keywordId, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/initialize")
  public ResponseEntity<Void> initialize() {
    contentSearchService.initializeIndexWithAllContents();
    return ResponseEntity.noContent().build();
  }

}
