package team03.mopl.domain.curation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.common.CursorPageResponse;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.entity.ContentWithScore;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.repository.KeywordRepository;

@Service
@RequiredArgsConstructor
public class KeywordRecommentServiceImpl implements KeywordRecommendService {

  private ContentRepository contentRepository;
  private KeywordRepository keywordRepository;

  @Override
  public CursorPageResponse<Content> getKeywordRecommendations(UUID userId, String cursor, int size) {
    List<Keyword> keywords = keywordRepository.findAllByUserId(userId);

    if (keywords.isEmpty()) {
      return getPopularContents(cursor, size);
    }

    Map<UUID, Double> contentScores = new HashMap<>();

    for (Keyword keyword : keywords) {
      List<Content> matchingContents = contentRepository.findByKeywordMatching(keyword.getKeyword());

      for (Content content : matchingContents) {
        double score = calculateKeywordScore(content, keyword.getKeyword());
        contentScores.merge(content.getId(), score, Double::sum);
      }
    }

    List<ContentWithScore> scoredContents = contentScores.entrySet().stream()
        .map(entry -> {
          Content content = contentRepository.findById(entry.getKey()).orElse(null);
          return content != null ? new ContentWithScore(content, entry.getValue()) : null;
        })
        .filter(Objects::nonNull)
        .sorted((a, b) -> {
          int scoreCompare = Double.compare(b.getScore(), a.getScore());
          return scoreCompare != 0 ? scoreCompare : a.getContent().getId().compareTo(b.getContent()
              .getId());
        })
        .toList();

    return applyCursorPagination(scoredContents, cursor, size);
  }

  @Override
  public CursorPageResponse<Content> searchByKeyword(String keyword, String cursor, int size) {
    List<Content> results = contentRepository.findByKeywordMatching(keyword);

    List<ContentWithScore> scoredContents = results.stream()
        .map(content-> new ContentWithScore(content, calculateKeywordScore(content, keyword)))
        .sorted((a, b) -> {
          int scoreCompare = Double.compare(b.getScore(), a.getScore());
          return scoreCompare != 0 ? scoreCompare : a.getContent().getId().compareTo(b.getContent()
              .getId());
        })
        .toList();

    return applyCursorPagination(scoredContents, cursor, size);
  }

  @Override
  public CursorPageResponse<Content> searchByMultipleKeywords(List<String> keywords, String cursor, int size) {
    Map<UUID, Double> contentScores = new HashMap<>();

    for (String keyword : keywords) {
      List<Content> matchingContents = contentRepository.findByKeywordMatching(keyword);

      for (Content content : matchingContents) {
        double score = calculateKeywordScore(content, keyword);
        contentScores.merge(content.getId(), score, Double::sum);
      }
    }

    List<ContentWithScore> scoreContents = contentScores.entrySet().stream()
        .map(entry -> {
          Content content = contentRepository.findById(entry.getKey()).orElse(null);
          return content != null ? new ContentWithScore(content, entry.getValue()) : null;
        })
        .filter(Objects::nonNull)
        .sorted((a, b) -> {
          int scoreCompare = Double.compare(b.getScore(), a.getScore());
          return scoreCompare != 0 ? scoreCompare : a.getContent().getId().compareTo(b.getContent()
              .getId());
        })
        .toList();

    return applyCursorPagination(scoreContents, cursor, size);
  }

  @Override
  public CursorPageResponse<Content> getSimilarContents(UUID contentId, String cursor, int size) {
    Content content = contentRepository.findById(contentId).orElse(null);
    if (content == null) {
      return new CursorPageResponse<>(new ArrayList<>(), null, null, 0, 0L, false);
    }

    List<String> keywords = new ArrayList<>();

    if (content.getTitle() != null) {
      keywords.addAll(extractKeywordFromText(content.getTitle(), 5));
    }

    if (content.getDescription() != null) {
      keywords.addAll(extractKeywordFromText(content.getDescription(), 5));
    }

    if (keywords.isEmpty()) {
      return getPopularContents(cursor, size);
    }

    CursorPageResponse<Content> results = searchByMultipleKeywords(keywords, cursor, size+1);

    // 자기 자신 콘텐츠 제외
    List<Content> filterContent = results.content().stream()
        .filter(content1 -> !content.getId().equals(contentId))
        .limit(size)
        .toList();

    long totalElements = results.totalElements() > 0 ? results.totalElements() - 1 : 0;

    return new CursorPageResponse<>(
        filterContent,
        results.nextCursor(),
        results.nextAfter(),
        filterContent.size(),
        totalElements,
        results.hasNext()
    );
  }

  @Override
  public CursorPageResponse<Content> getPopularContents(String cursor, int size) {
    List<Content> allContents = contentRepository.findTopByOrderByAvgRatingDescViewingCountDesc();

    List<ContentWithScore> scoreContents = allContents.stream()
        .map(content -> {
          double score =
              content.getAvgRating().doubleValue() * 2.0 + Math.log(content.getViewingCount() + 1) * 0.5;
          return new ContentWithScore(content, score);
        })
        .sorted((a, b) -> {
          int scoreCompare = Double.compare(b.getScore(), a.getScore());
          return scoreCompare != 0 ? scoreCompare : a.getContent().getId().compareTo(b.getContent()
              .getId());
        })
        .toList();

    return applyCursorPagination(scoreContents, cursor, size);
  }

  private CursorPageResponse<Content> applyCursorPagination(List<ContentWithScore> scoreContents, String cursor, int size) {
    int startIdx = 0;

    if (cursor != null && !cursor.isEmpty()) {
      for (int i = 0; i < scoreContents.size(); i++) {
        if (scoreContents.get(i).getCursor().compareTo(cursor) < 0) {
          startIdx = i;
          break;
        }
      }
    }

    int endIdx = Math.min(startIdx + size + 1, scoreContents.size());
    List<ContentWithScore> pageContents = scoreContents.subList(startIdx, endIdx);

    boolean hasNext = pageContents.size() > size;
    String nextCursor = null;
    Instant nextAfter = Instant.now();

    if (hasNext) {
      pageContents = pageContents.subList(0, size);
      nextCursor = pageContents.get(pageContents.size() - 1).getCursor();
    }

    List<Content> contents = pageContents.stream()
        .map(ContentWithScore::getContent)
        .toList();

    long totalElements = scoreContents.size();

    return new CursorPageResponse<>(
        contents,
        nextCursor,
        nextAfter,
        contents.size(),
        totalElements,
        hasNext
    );
  }

  private double calculateKeywordScore(Content content, String keyword) {
    double score = 0.0;
    String lowerKeyword = keyword.toLowerCase();

    if (content.getTitle() != null && content.getTitle().toLowerCase().contains(lowerKeyword)) {
      score += 15.0;
    }

    if (content.getDescription() != null && content.getDescription().toLowerCase().contains(lowerKeyword)) {
      score += 8.0;
    }

    if (content.getAvgRating() != null) {
      score += content.getAvgRating().doubleValue() * 2.0;
    }

    score += Math.log(content.getViewingCount() + 1) * 0.5;
    return score;
  }

  @Override
  public List<String> extractKeywordFromText(String text, int maxKeywords) {
    if (text == null || text.trim().isEmpty()) {
      return new ArrayList<>();
    }

    Map<String, Integer> wordCount = new HashMap<>();

    String[] words = text.toLowerCase()
        .replaceAll("[^가-힣a-zA-Z0-9\\s]", "")
        .split("\\s");

    for (String word : words) {
      if (word.length() > 1) {
        wordCount.merge(word, 1, Integer::sum);
      }
    }

    return wordCount.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(maxKeywords)
        .map(Map.Entry::getKey)
        .toList();
  }
}