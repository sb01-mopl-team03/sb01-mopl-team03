package team03.mopl.domain.curation.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import team03.mopl.domain.curation.service.CurationService;

@RestController
@RequestMapping("/api/curations")
@RequiredArgsConstructor
public class CurationController {

  private final CurationService curationService;

  @PostMapping("/keywords")
  public ResponseEntity<Keyword> registerKeyword(@RequestBody KeywordRequest request) {
    Keyword keyword = curationService.registerKeyword(request.userId(), request.keyword());
    return ResponseEntity.ok(keyword);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<Content>> getRecommendations(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "10") int limit) {
    List<Content> recommendations = curationService.getRecommendationsForUser(userId, limit);
    return ResponseEntity.ok(recommendations);
  }

}
