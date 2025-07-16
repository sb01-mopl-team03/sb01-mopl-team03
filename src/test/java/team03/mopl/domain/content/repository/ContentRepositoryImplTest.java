package team03.mopl.domain.content.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import team03.mopl.common.config.JpaConfig;
import team03.mopl.common.config.QueryDslConfig;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;


/**
 * ContentRepository의 QueryDSL 기능에 대한 단위 테스트
 * <p>
 * shema.sql의 영향을 받지 않게 설정하고 오직 최신 엔티티@Entity 정의만을 기준으로 테스트하기 위해 아래 설정을 사용했습니다.
 *
 * @TestPropertySource: application.yaml 보다 높은 우선순위를 가지고 설정 덮어씁니다.
 */
@Import({QueryDslConfig.class, JpaConfig.class})
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never", // schema.sql 자동 실행 막음
    "spring.jpa.hibernate.ddl-auto=create-drop" // 내장 DB에 테이블을 자동으로 생성/삭제
})
@DisplayName("컨텐츠 데이터 레포지토리 단위 테스트")
class ContentRepositoryImplTest {

  @Autowired
  private ContentRepository contentRepository;

  /**
   * 공용 데이터 생성
   */
  @BeforeEach
  void setUp() {
  }


  @Nested
  @DisplayName("커서 페이지네이션 기반의 컨텐츠 데이터 조회")
  class findContentsWithCursor {

    @Nested
    @DisplayName("기본 필터링 테스트")
    class filtering {

      @Test
      @DisplayName("제목에 키워드가 포함된 콘텐츠를 정상적으로 조회")
      void returnResultsWhenTitleContainsKeyword() {
        // given
        List<Content> contents = List.of(
            Content.builder().title("제목필터링테스트1-검색됨").titleNormalized("제목필터링테스트1-검색됨")
                .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
                .contentType(ContentType.MOVIE)
                .youtubeUrl("").build(),
            Content.builder().title("제목필터링테스트2-검색됨").titleNormalized("제목필터링테스트2-검색됨")
                .releaseDate(LocalDateTime.parse("2019-07-09T10:00"))
                .contentType(ContentType.MOVIE)
                .youtubeUrl("").build(),
            Content.builder().title("제목필터링테스트3-안됨").titleNormalized("제목필터링테스트3-안됨")
                .releaseDate(LocalDateTime.parse("2019-07-09T10:00"))
                .contentType(ContentType.MOVIE)
                .youtubeUrl("").build()
        );
        contentRepository.saveAll(contents);

        String title = "검색됨";
        String contentType = null;
        String sortBy = "TITLE";
        String direction = "DESC";
        String cursor = null;
        UUID cursorId = null;
        int size = 10;

        // when
        List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
            direction, cursor, cursorId, size);

        // then
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).allMatch(content -> content.getTitle().contains("검색됨"));
      }

      @Test
      @DisplayName("컨텐츠 타입을 MOVIE 로 지정했을 때 MOVIE 타입의 데이터를 정상적으로 조회")
      void returnResultsWhenContentTypeIsMovie() {
        // given
        List<Content> contents = List.of(
            Content.builder().title("컨텐츠타입필터링1").titleNormalized("컨텐츠타입필터링1")
                .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
                .contentType(ContentType.MOVIE)
                .youtubeUrl("").build(),
            Content.builder().title("컨텐츠타입필터링2").titleNormalized("컨텐츠타입필터링2")
                .releaseDate(LocalDateTime.parse("2019-07-09T10:00"))
                .contentType(ContentType.MOVIE)
                .youtubeUrl("").build(),
            Content.builder().title("컨텐츠타입필터링3").titleNormalized("컨텐츠타입필터링3")
                .releaseDate(LocalDateTime.parse("2019-07-09T10:00")).contentType(ContentType.TV)
                .youtubeUrl("").build()
        );
        contentRepository.saveAll(contents);

        String title = null;
        String contentType = "MOVIE";
        String sortBy = "TITLE";
        String direction = "DESC";
        String cursor = null;
        UUID cursorId = null;
        int size = 10;

        // when
        List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
            direction, cursor, cursorId, size);

        // then
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).allMatch(content -> content.getContentType().equals(ContentType.MOVIE));
      }

      @Test
      @DisplayName("컨텐츠 데이터에 존재하지 않는 영화의 제목을 검색했을 때 빈 리스트 반환")
      void returnEmptyResultWhenTitleNotExist() {
        // given
        List<Content> contents = List.of(
            Content.builder().title("존재하는데이터").titleNormalized("존재하는데이터")
                .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
                .contentType(ContentType.MOVIE).youtubeUrl("").build()
        );
        contentRepository.saveAll(contents);

        String title = "없는";
        String contentType = "MOVIE";
        String sortBy = "TITLE";
        String direction = "DESC";
        String cursor = null;
        UUID cursorId = null;
        int size = 10;

        // when
        List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
            direction, cursor, cursorId, size);

        // then
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(0);
      }
    }
  }

  @Nested
  @DisplayName("정렬 테스트")
  class order {

    @Test
    @DisplayName("컨텐츠 데이터가 제목 기준의 내림차순으로 정렬했을 때 정상적으로 정렬")
    void returnResultsWhenSortByTitle() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("1").titleNormalized("1")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("2").titleNormalized("2")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("A").titleNormalized("A")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("B").titleNormalized("B")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("가").titleNormalized("가")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("나").titleNormalized("나")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      String title = null;
      String contentType = "MOVIE";
      String sortBy = "TITLE";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 10;

      // when
      List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(results).isNotNull();
      assertThat(results.size()).isEqualTo(6);
      assertThat(results)
          .extracting(Content::getTitle) // List<String> 타입의 새로운 리스트 반환
          .containsExactly("나", "가", "B", "A", "2", "1");
    }

    @Test
    @DisplayName("컨텐츠 데이터가 릴리즈 날짜 기준의 내림차순으로 정렬했을 때 정상적으로 정렬")
    void returnResultsWhenSortByDate() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-05-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-04-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-03-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-02-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      String title = null;
      String contentType = "MOVIE";
      String sortBy = "RELEASE_AT";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 10;

      // when
      List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(results).isNotNull();
      assertThat(results.size()).isEqualTo(6);
      assertThat(results)
          .extracting(content -> content.getReleaseDate().toString())
          .containsExactly("2025-07-07T10:00", "2025-06-07T10:00", "2025-05-07T10:00",
              "2025-04-07T10:00", "2025-03-07T10:00", "2025-02-07T10:00");
    }

    @Test
    @DisplayName("컨텐츠 데이터가 평균 별점 기준의 내림차순으로 정렬했을 때 정상적으로 정렬")
    void returnResultsWhenSortByAvgRating() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(4.5)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(2)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(3.5)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(4)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(2.5)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-16T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(3)).build()
      );
      contentRepository.saveAll(contents);

      String title = null;
      String contentType = "MOVIE";
      String sortBy = "AVG_RATING";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 10;

      // when
      List<Content> results = contentRepository.findContentsWithCursor(title, contentType, sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(results).isNotNull();
      assertThat(results.size()).isEqualTo(6);
      assertThat(results)
          .extracting(content -> content.getAvgRating())
          .containsExactly(BigDecimal.valueOf(4.5), BigDecimal.valueOf(4), BigDecimal.valueOf(3.5),
              BigDecimal.valueOf(3), BigDecimal.valueOf(2.5), BigDecimal.valueOf(2));
    }

    @Test
    @DisplayName("컨텐츠 데이터 내림차순 오름차순 테스트")
    void returnResultsDescAndAsc() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("1").titleNormalized("1")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("2").titleNormalized("2")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("A").titleNormalized("A")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("B").titleNormalized("B")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("가").titleNormalized("가")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("나").titleNormalized("나")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      String title = null;
      String contentType = "MOVIE";
      String sortBy = "TITLE";
      String cursor = null;
      UUID cursorId = null;
      int size = 10;

      // 내림차순
      String directionDesc = "DESC";

      // when
      List<Content> resultsDesc = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          directionDesc, cursor, cursorId, size);

      // then
      assertThat(resultsDesc).isNotNull();
      assertThat(resultsDesc.size()).isEqualTo(6);
      assertThat(resultsDesc)
          .extracting(Content::getTitle)
          .containsExactly("나", "가", "B", "A", "2", "1");

      // 오름차순
      String directionAsc = "ASC";

      // when
      List<Content> resultsAsc = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          directionAsc, cursor, cursorId, size);

      // then
      assertThat(resultsAsc).isNotNull();
      assertThat(resultsAsc.size()).isEqualTo(6);
      assertThat(resultsAsc)
          .extracting(Content::getTitle)
          .containsExactly("1", "2", "A", "B", "가", "나");
    }
  }

  @Nested
  @DisplayName("커서 테스트")
  class cursor {

    @Test
    @DisplayName("제목 기준 내림차순 정렬 시, 커서를 이용해 첫번째, 두번째 페이지를 정상적으로 조회")
    void returnResultTitleDescCursorPaging() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("Ç").titleNormalized("C")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("B-").titleNormalized("B")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          //
          Content.builder().title("A").titleNormalized("A")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      // 첫번째 페이지
      String title = null;
      String contentType = "MOVIE";
      String sortBy = "TITLE";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 2;

      // when
      List<Content> firstPageresults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(firstPageresults).isNotNull();
      assertThat(firstPageresults.size()).isEqualTo(2);
      assertThat(firstPageresults)
          .extracting(Content::getTitle)
          .containsExactly("Ç", "B-");

      // 두번째 페이지
      // given
      Content lastContentFirstPage = firstPageresults.get(size - 1);
      String nextCursor = lastContentFirstPage.getTitleNormalized();
      UUID nextCursorId = lastContentFirstPage.getId();

      // when
      List<Content> secondPageResults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, nextCursor, nextCursorId, size);

      // then
      assertThat(secondPageResults).isNotNull();
      assertThat(secondPageResults.size()).isEqualTo(1);
      assertThat(secondPageResults)
          .extracting(Content::getTitle)
          .containsExactly("A");
    }

    @Test
    @DisplayName("릴리즈 날짜 기준 내림차순 정렬 시, 커서를 이용해 첫번째, 두번째 페이지를 정상적으로 조회")
    void returnResultReleaseAtDescCursorPaging() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-06T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          //
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-08T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      // 첫번째 페이지
      String title = null;
      String contentType = "MOVIE";
      String sortBy = "RELEASE_AT";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 2;

      // when
      List<Content> firstPageresults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(firstPageresults).isNotNull();
      assertThat(firstPageresults.size()).isEqualTo(2);
      assertThat(firstPageresults)
          .extracting(Content::getReleaseDate)
          .containsExactly(LocalDateTime.parse("2025-07-08T10:00"), LocalDateTime.parse("2025-07-07T10:00"));

      // 두번째 페이지
      // given
      Content lastContentFirstPage = firstPageresults.get(size - 1);
      String nextCursor = lastContentFirstPage.getReleaseDate().toString();
      UUID nextCursorId = lastContentFirstPage.getId();

      // when
      List<Content> secondPageResults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, nextCursor, nextCursorId, size);

      // then
      assertThat(secondPageResults).isNotNull();
      assertThat(secondPageResults.size()).isEqualTo(1);
      assertThat(secondPageResults)
          .extracting(Content::getReleaseDate)
          .containsExactly(LocalDateTime.parse("2025-07-06T10:00"));
    }


    @Test
    @DisplayName("평균 별점 기준 내림차순 정렬 시, 커서를 이용해 첫번째, 두번째 페이지를 정상적으로 조회")
    void returnResultAvgRatingDescCursorPaging() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(3)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(5)).build(),
          //
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(4)).build()
      );
      contentRepository.saveAll(contents);

      // 첫번째 페이지
      String title = null;
      String contentType = "MOVIE";
      String sortBy = "AVG_RATING";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 2;

      // when
      List<Content> firstPageresults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, cursor, cursorId, size);

      // then
      assertThat(firstPageresults).isNotNull();
      assertThat(firstPageresults.size()).isEqualTo(2);
      assertThat(firstPageresults)
          .extracting(Content::getAvgRating)
          .containsExactly(BigDecimal.valueOf(5), BigDecimal.valueOf(4));

      // 두번째 페이지
      // given
      Content lastContentFirstPage = firstPageresults.get(size - 1);
      String nextCursor = lastContentFirstPage.getAvgRating().toString();
      UUID nextCursorId = lastContentFirstPage.getId();

      // when
      List<Content> secondPageResults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, nextCursor, nextCursorId, size);

      // then
      assertThat(secondPageResults).isNotNull();
      assertThat(secondPageResults.size()).isEqualTo(1);
      assertThat(secondPageResults)
          .extracting(Content::getAvgRating)
          .containsExactly(BigDecimal.valueOf(3));
    }

    @Test
    @DisplayName("평균 별점 기준 동점자 처리 확인. 내림차순 정렬 시, 커서를 이용해 첫번째, 두번째 페이지를 정상적으로 조회")
    void returnResultTiedAvgRatingDescCursorPaging() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(4)).build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(5)).build(),
          //
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-07-07T10:00")).contentType(ContentType.MOVIE)
              .youtubeUrl("").avgRating(BigDecimal.valueOf(4)).build()
      );
      contentRepository.saveAll(contents);

      // 첫번째 페이지
      String title = null;
      String contentType = "MOVIE";
      String sortBy = "AVG_RATING";
      String direction = "DESC";
      String cursor = null;
      UUID cursorId = null;
      int size = 2;

      // when
      List<Content> firstPageresults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, cursor, cursorId, size);


      // 두번째 페이지
      // given
      Content lastContentFirstPage = firstPageresults.get(size - 1);
      String nextCursor = lastContentFirstPage.getAvgRating().toString();
      UUID nextCursorId = lastContentFirstPage.getId();

      // when
      List<Content> secondPageResults = contentRepository.findContentsWithCursor(title, contentType,
          sortBy,
          direction, nextCursor, nextCursorId, size);

      //
      System.out.println("First page last ID   : " + lastContentFirstPage.getId());
      System.out.println("Second page first ID : " + secondPageResults.get(0).getId());
      assertThat(lastContentFirstPage.getAvgRating()).isEqualTo(BigDecimal.valueOf(4));
      assertThat(secondPageResults.get(0).getAvgRating()).isEqualTo(BigDecimal.valueOf(4));
      assertThat(secondPageResults.get(0).getId().toString())
          .isLessThan(lastContentFirstPage.getId().toString());
    }
  }

  @Nested
  @DisplayName("컨텐츠 개수 조회 테스트")
  class count{
    @Test
    @DisplayName("제목에 키워드가 포함된 콘텐츠의 수를 정상적으로 조회")
    void returnCountResultsWhenTitleContainsKeyword() {
      // given
      List<Content> contents = List.of(
          Content.builder().title("제목필터링테스트1-검색됨").titleNormalized("제목필터링테스트1-검색됨")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("제목필터링테스트2-검색됨").titleNormalized("제목필터링테스트2-검색됨")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("제목필터링테스트3-안됨").titleNormalized("제목필터링테스트3-안됨")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build()
      );
      contentRepository.saveAll(contents);

      String title = "검색됨";
      String contentType = null;

      // when
      long countResult = contentRepository.countContentsWithFilter(title, contentType);

      // then
      assertThat(countResult).isEqualTo(2);
    }

    @Test
    @DisplayName("컨텐츠 타입을 지정했을 때 콘텐츠의 수를 정상적으로 조회")
    void returnCountResultsFilteredByContentType(){
      // given
      List<Content> contents = List.of(
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.TV)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.SPORTS)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.SPORTS)
              .youtubeUrl("").build(),
          Content.builder().title("더미").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.SPORTS)
              .youtubeUrl("").build()

      );
      contentRepository.saveAll(contents);

      String title = null;
      String contentType1 = "TV";
      String contentType2 = "MOVIE";
      String contentType3 = "SPORTS";

      // when
      long countResultTV = contentRepository.countContentsWithFilter(title, contentType1);
      long countResultMovie = contentRepository.countContentsWithFilter(title, contentType2);
      long countResultSports = contentRepository.countContentsWithFilter(title, contentType3);

      // then
      assertThat(countResultTV).isEqualTo(1);
      assertThat(countResultMovie).isEqualTo(2);
      assertThat(countResultSports).isEqualTo(3);
    }

    @Test
    @DisplayName("제목과 컨텐츠 타입 지정했을 때 콘텐츠의 수를 정상적으로 조회")
    void returnCountResultsFilteredByTitleAndContentType(){
      // given
      List<Content> contents = List.of(
          Content.builder().title("검색됨").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.TV)
              .youtubeUrl("").build(),
          Content.builder().title("검색되면안됨").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.MOVIE)
              .youtubeUrl("").build(),
          Content.builder().title("검색되면안됨").titleNormalized("더미")
              .releaseDate(LocalDateTime.parse("2025-06-25T10:00"))
              .contentType(ContentType.SPORTS)
              .youtubeUrl("").build()

      );
      contentRepository.saveAll(contents);

      String title = "검색";
      String contentType = "TV";

      // when
      long countResult = contentRepository.countContentsWithFilter(title, contentType);

      // then
      assertThat(countResult).isEqualTo(1);
    }

  }
}

