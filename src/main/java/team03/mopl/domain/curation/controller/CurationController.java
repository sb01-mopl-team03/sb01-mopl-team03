package team03.mopl.domain.curation.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.curation.service.CurationService;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class CurationController {

  private final CurationService curationService;
  private final KeywordRepository keywordRepository;

  @PostMapping()
  public ResponseEntity<Keyword> registerKeyword(@RequestBody KeywordRequest request) {
    Keyword keyword = curationService.registerKeyword(request.userId(), request.keyword());
    return ResponseEntity.ok(keyword);
  }

  @GetMapping("/{keywordId}/contents")
  public ResponseEntity<List<Content>> getRecommendations(
      @PathVariable UUID keywordId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = UUID.fromString(userDetails.getUsername());

    List<Content> recommendations = curationService.getRecommendationsByKeyword(keywordId, userId);
    return ResponseEntity.ok(recommendations);
  }

  @DeleteMapping("/{keywordId}")
  public ResponseEntity<Void> delete(@PathVariable UUID keywordId) {
    keywordService.delete(keywordId);
  }

}
