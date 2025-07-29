package team03.mopl.domain.watchroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.util.SpringApplicationContext;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.user.User;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithParticipantCountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;

import java.util.List;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class, SpringApplicationContext.class})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.defer-datasource-initialization=true",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:sql/watchroom-test-data.sql"
})
@DisplayName("시청방-사용자 레포지토리 단위 테스트")
class WatchRoomParticipantRepositoryTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private WatchRoomParticipantRepository watchRoomParticipantRepository;

  // 기본 테스트 데이터 ID
  private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private final UUID contentId = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private final UUID watchRoomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private final UUID watchRoomParticipantId = UUID.fromString("66666666-6666-6666-6666-666666666666");

  // 페이지네이션 테스트용 시청방 ID (SQL 스크립트에 정의된)
  private final UUID room0Id = UUID.fromString("55555555-5555-5555-5555-555555555550");
  private final UUID room4Id = UUID.fromString("55555555-5555-5555-5555-555555555554");
  private final UUID room7Id = UUID.fromString("55555555-5555-5555-5555-555555555557");

  private User owner;
  private User user;
  private Content content;
  private WatchRoom watchRoom;
  private WatchRoomParticipant watchRoomParticipantForOwner;

  // 페이지네이션 테스트용 시청방 및 날짜
  private WatchRoom room4;
  private WatchRoom room7;
  private LocalDateTime dateCursor;
  private String titleCursor;

  @BeforeEach
  void setUp() {
    // 각 테스트 실행 전 EntityManager 캐시 초기화
    em.clear();

    // SQL 스크립트에서 생성된 기본 엔티티들을 가져옴
    owner = em.find(User.class, ownerId);
    user = em.find(User.class, userId);
    content = em.find(Content.class, contentId);
    watchRoom = em.find(WatchRoom.class, watchRoomId);

    // 페이지네이션 테스트용 시청방 조회
    room4 = em.find(WatchRoom.class, room4Id);
    room7 = em.find(WatchRoom.class, room7Id);

    if (room4 != null) {
      dateCursor = room4.getCreatedAt();
      titleCursor = room4.getTitle();
    }

    // 참여 정보 가져오기
    watchRoomParticipantForOwner = watchRoomParticipantRepository
        .findByUserAndWatchRoom(owner, watchRoom)
        .orElseThrow(() -> new IllegalStateException("Owner participant not found"));
  }

  @Nested
  @DisplayName("사용자와 시청방으로 조회")
  class FindByUserAndWatchRoomTest {

    @Test
    @DisplayName("성공")
    void success() {
      //when
      WatchRoomParticipant byUserAndWatchRoom = watchRoomParticipantRepository.findByUserAndWatchRoom(
          owner, watchRoom).orElse(null);

      //then
      assertNotNull(byUserAndWatchRoom);
      assertEquals(byUserAndWatchRoom.getUser(), owner);
      assertEquals(byUserAndWatchRoom.getWatchRoom(), watchRoom);
    }

    @Test
    @DisplayName("유저가 해당 시청방에 참여하지 않음")
    void failWhenUserNotFound() {
      //when
      WatchRoomParticipant byUserAndWatchRoom = watchRoomParticipantRepository.findByUserAndWatchRoom(
          user, watchRoom).orElse(null);
      //then
      assertNull(byUserAndWatchRoom);
    }
  }

  @Nested
  @DisplayName("유저가 해당 시청방에 참여중인지 확인")
  class ExistsWatchRoomParticipantByWatchRoomAndUserTest {

    @Test
    @DisplayName("참여중")
    void joinedWatchRoom() {
      //when
      boolean joined = watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(
          watchRoom, owner);

      //then
      assertTrue(joined);
    }

    @Test
    @DisplayName("참여하지 않음")
    void notInWatchRoom() {
      //when
      boolean joined = watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(
          watchRoom, user);

      //then
      assertFalse(joined);
    }
  }

  @Nested
  @DisplayName("실시간 시청방 참여 정보 중 가장 첫번째 정보 찾기")
  class FindFirstByWatchRoomTest {

    @Test
    @DisplayName("참여중인 유저가 있음")
    void findWatchRoomParticipant() {
      //when
      WatchRoomParticipant byUserAndWatchRoom = watchRoomParticipantRepository.findFirstByWatchRoom(
          watchRoom).orElse(null);
      //then
      assertNotNull(byUserAndWatchRoom);
    }

    @Test
    @DisplayName("참여중인 유저가 없음")
    void noOneInWatchRoom() {
      //given
      watchRoomParticipantRepository.delete(watchRoomParticipantForOwner);

      //when
      WatchRoomParticipant byUserAndWatchRoom = watchRoomParticipantRepository.findFirstByWatchRoom(
          watchRoom).orElse(null);
      //then
      assertNull(byUserAndWatchRoom);
    }
  }

  @Nested
  @DisplayName("시청방으로 참여 정보 조회")
  class FindAllByWatchRoomTest {

    @Test
    @DisplayName("참여자 있음")
    void findAllByWatchRoom() {
      //when
      List<WatchRoomParticipant> byWatchRoom = watchRoomParticipantRepository.findByWatchRoom(
          watchRoom);
      //then
      assertEquals(1, byWatchRoom.size());
      assertEquals(byWatchRoom.get(0).getUser(), owner);
    }

    @Test
    @DisplayName("참여자 없음")
    void noOneInWatchRoom() {
      //given
      watchRoomParticipantRepository.delete(watchRoomParticipantForOwner);

      //when
      List<WatchRoomParticipant> byWatchRoom = watchRoomParticipantRepository.findByWatchRoom(
          watchRoom);

      //then
      assertEquals(0, byWatchRoom.size());
    }

    @Test
    @DisplayName("참여자 2명 이상")
    void moreThenTwoParticipants() {
      //given
      // SQL 스크립트에서 생성된 room7 사용 (이미 owner, user 둘 다 참여)
      WatchRoom testRoom = em.find(WatchRoom.class, room7Id);

      //when
      List<WatchRoomParticipant> byWatchRoom = watchRoomParticipantRepository.findByWatchRoom(
          testRoom);

      //then
      assertEquals(2, byWatchRoom.size());
    }
  }

  @Nested
  @DisplayName("검색 결과 총 개수 조회")
  class CountWatchRoomContentWithHeadcountDtoTest {

    @Test
    @DisplayName("시청방 제목에 검색어가 있음")
    void whenWatchRoomTitleContainsKeyword() {
      //given
      String keyword = "장그래";

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertEquals(1L, total);
    }

    @Test
    @DisplayName("시청방 소유자 이름에 검색어가 있음")
    void whenWatchRoomOwnerNameContainsKeyword() {
      //given
      String keyword = "ne"; //owner

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertTrue(total >= 1L);
    }

    @Test
    @DisplayName("컨텐츠 제목에 검색어가 있음")
    void whenWatchRoomContentTitleContainsKeyword() {
      //given
      String keyword = "미";

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertTrue(total >= 1L);
    }

    @Test
    @DisplayName("시청방 제목, 컨텐츠 제목, 소유자 이름 모두에 키워드가 들어가지 않음")
    void whenNothingContainsKeyword() {
      //given
      String keyword = "존재하지않는키워드";

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertEquals(0L, total);
    }
  }

  @Nested
  @DisplayName("시청방 정보 개별 조회")
  class GetWatchRoomContentWithHeadcountDtoTest {

    @Test
    @DisplayName("조회 결과 있음")
    void success() {
      //given
      // room7는 참여자가 2명인 시청방
      UUID testRoomId = room7Id;

      //when
      WatchRoomContentWithParticipantCountDto result = watchRoomParticipantRepository
          .getWatchRoomContentWithHeadcountDto(testRoomId)
          .orElse(null);

      //then
      assertNotNull(result);
      assertEquals(testRoomId, result.getWatchRoom().getId());
      assertEquals(user, result.getWatchRoom().getOwner());
      assertEquals(content, result.getContent());
      assertEquals(2L, result.getParticipantCount().longValue());
    }

    @Test
    @DisplayName("조회 결과 없음")
    void notFound() {
      //given
      UUID randomUUID = UUID.randomUUID();

      //when
      WatchRoomContentWithParticipantCountDto result = watchRoomParticipantRepository
          .getWatchRoomContentWithHeadcountDto(randomUUID)
          .orElse(null);

      //then
      assertNull(result);
    }

    @Test
    @DisplayName("참여자가 없는 경우 테스트")
    void noOneInWatchRoom() {
      //given
      // 새로운 시청방 생성 (참여자 없음) - ID 직접 설정 안함
      WatchRoom newWatchRoom = WatchRoom.builder()
          .title("참여자없는방")
          .owner(owner)
          .content(content)
          .createdAt(LocalDateTime.now())
          .build();
      em.persist(newWatchRoom);
      em.flush();

      UUID newWatchRoomId = newWatchRoom.getId(); // 저장 후 생성된 ID 획득

      //when
      WatchRoomContentWithParticipantCountDto result = watchRoomParticipantRepository
          .getWatchRoomContentWithHeadcountDto(newWatchRoomId)
          .orElse(null);

      //then
      assertNotNull(result);
      assertEquals(newWatchRoomId, result.getWatchRoom().getId());
      assertEquals(owner, result.getWatchRoom().getOwner());
      assertEquals(content, result.getContent());
      assertEquals(0L, result.getParticipantCount().longValue());
    }
  }

  @Nested
  @DisplayName("검색 결과 페이지네이션 조회")
  class GetAllWatchRoomContentWithHeadcountDtoPaginatedTest {

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, Title, ASC, Cursor null, size 20")
    void whenWatchRoomTitleContainsKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("테스")
          .sortBy("title")
          .direction("asc")
          .cursor(cursor)
          .size(20)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      assertTrue(result.get(0).getWatchRoom().getTitle().contains("테스"));
    }

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, Title, ASC, Cursor not null, size 20")
    void whenWatchRoomTitleContainsKeywordWithTitleCursor() {
      //given
      String lastValue = titleCursor;
      String lastId = room4Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("테스")
          .sortBy("title")
          .direction("asc")
          .cursor(cursor)
          .size(20)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      // 결과의 첫 번째 항목은 테스트시청방4보다 큰 항목이어야 함
      assertTrue(result.get(0).getWatchRoom().getTitle().compareTo("테스트시청방4") > 0);
    }

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, Title, Desc, Cursor not null, size 20")
    void whenWatchRoomTitleContainsKeywordWithTitleCursorDesc() {
      //given
      String lastValue = titleCursor;
      String lastId = room4Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("테스")
          .sortBy("title")
          .direction("desc")
          .cursor(cursor)
          .size(20)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      // 결과의 첫 번째 항목은 테스트시청방4보다 작은 항목이어야 함
      assertTrue(result.get(0).getWatchRoom().getTitle().compareTo("테스트시청방4") < 0);
    }

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, createdAt, desc, Cursor not null, size 10")
    void whenWatchRoomTitleContainsKeywordSortedByCreatedAt() {
      //given
      String lastValue = dateCursor.toString();
      String lastId = room4Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("시청방")
          .sortBy("createdAt")
          .direction("desc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      assertTrue(result.get(0).getWatchRoom().getTitle().contains("시청방"));
      // 결과의 첫 번째 항목은 테스트시청방4보다 이후에 생성된 항목이어야 함
      assertTrue(result.get(0).getWatchRoom().getCreatedAt().isBefore(dateCursor));
    }

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, createdAt, asc, Cursor not null, size 10")
    void whenWatchRoomTitleContainsKeywordSortedByCreatedAtAsc() {
      //given
      String lastValue = dateCursor.toString();
      String lastId = room4Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("시청방")
          .sortBy("createdAt")
          .direction("asc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      assertTrue(result.get(0).getWatchRoom().getTitle().contains("시청방"));
      // 결과의 첫 번째 항목은 테스트시청방4보다 이후에 생성된 항목이어야 함
      assertTrue(result.get(0).getWatchRoom().getCreatedAt().isAfter(dateCursor));
    }

    @Test
    @DisplayName("시청방 소유자 이름에 검색어가 있음, participantCount, Desc, Cursor not null, size 10")
    void whenFilteringByParticipantCountCursorDesc() {
      //given
      String lastValue = "2";
      String lastId = room7Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("er") // owner 또는 user에 포함
          .sortBy("participantCount")
          .direction("desc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);
      //then
      assertTrue(result.size() > 0);
      // 참가자 수가 2명인 다른 방들이 나와야 함
    }

    @Test
    @DisplayName("시청방 소유자 이름에 검색어가 있음, Title, DESC, Cursor null, size 9")
    void whenWatchRoomOwnerNameContainsKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("use") // user에 포함
          .sortBy("title")
          .direction("DESC")
          .cursor(cursor)
          .size(9)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("컨텐츠 제목에 검색어가 있음, createdAt, ASC, Cursor not null, size 10")
    void whenWatchRoomContentTitleContainsKeyword() {
      //given
      String lastValue = dateCursor.toString();
      String lastId = room4Id.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("미") // 미생에 포함
          .sortBy("createdAt")
          .direction("asc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
      assertTrue(result.get(0).getContent().getTitle().contains("미"));
    }

    @Test
    @DisplayName("시청방 제목, 컨텐츠 제목, 소유자 이름 모두에 키워드가 들어가지 않음")
    void whenNothingContainsKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("존재하지않는키워드")
          .sortBy("createdAt")
          .direction("ASC")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("키워드 없음, 정렬 조건 없음, cursor null")
    void whenNoKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .direction("asc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertTrue(result.size() > 0);
    }
  }
}