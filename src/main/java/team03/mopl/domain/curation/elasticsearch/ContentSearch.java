package team03.mopl.domain.curation.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import team03.mopl.domain.content.Content;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "contents_search")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentSearch {

  @Id
  @Field(type = FieldType.Keyword)
  private String id;

  @Field(type = FieldType.Text)
  private String title;

  @Field(type = FieldType.Text)
  private String description;

  @Field(type = FieldType.Keyword)
  private String contentType;

  @Field(type = FieldType.Date)
  private String releaseDate;

  @Field(type = FieldType.Double)
  private Double avgRating;

  @Field(type = FieldType.Keyword, index = false)
  private String youtubeUrl;

  @Field(type = FieldType.Keyword, index = false)
  private String thumbnailUrl;

  // Content에서 ContentSearch로 변환
  public static ContentSearch from(Content content) {
    return ContentSearch.builder()
        .id(content.getId().toString())
        .title(content.getTitle())
        .description(content.getDescription())
        .contentType(content.getContentType().toString())
        .releaseDate(content.getReleaseDate().toString())
        .avgRating(content.getAvgRating().doubleValue())
        .youtubeUrl(content.getYoutubeUrl())
        .thumbnailUrl(content.getThumbnailUrl())
        .build();
  }
}
