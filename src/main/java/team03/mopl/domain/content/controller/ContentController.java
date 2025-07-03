package team03.mopl.domain.content.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.service.ContentService;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

  private final ContentService contentService;

  @GetMapping
  public ResponseEntity<List<ContentDto>> getAll(){
    return ResponseEntity.ok(contentService.getAll());
  }
}
