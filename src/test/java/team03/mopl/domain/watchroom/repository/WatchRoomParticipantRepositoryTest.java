package team03.mopl.domain.watchroom.repository;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import team03.mopl.common.config.QueryDslConfig;
import team03.mopl.common.dto.Cursor;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithParticipantCountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;

@DataJpaTest
@Import({WatchRoomParticipantRepositoryImpl.class, QueryDslConfig.class})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never", // schema.sql 자동 실행 막음
    "spring.jpa.hibernate.ddl-auto=create-drop" // 내장 DB에 테이블을 자동으로 생성/삭제
})
@DisplayName("시청방-사용자 레포지토리 단위 테스트")
class WatchRoomParticipantRepositoryTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private WatchRoomParticipantRepository watchRoomParticipantRepository;

  private User owner;
  private User user;
  private Content content;
  private WatchRoom watchRoom;
  private WatchRoomParticipant watchRoomParticipantForOwner;

  @BeforeEach
  void setUp() {
    owner = User.builder()
        .email("owner@test.com")
        .name("owner")
        .password("owner")
        .role(Role.USER)
        .isLocked(false)
        .isTempPassword(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    em.persist(owner);

    user = User.builder()
        .email("user@test.com")
        .name("user")
        .password("user")
        .role(Role.USER)
        .isLocked(false)
        .isTempPassword(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    em.persist(user);

    content = Content.builder()
        .title("미생")
        .titleNormalized("미생")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .youtubeUrl("https://www.youtube.com")
        .build();
    em.persist(content);

    watchRoom = WatchRoom.builder()
        .title("장그래 힘내라")
        .owner(owner)
        .content(content)
        .createdAt(LocalDateTime.now())
        .build();
    em.persist(watchRoom);

    watchRoomParticipantForOwner = WatchRoomParticipant.builder()
        .watchRoom(watchRoom)
        .user(owner)
        .createdAt(LocalDateTime.now())
        .build();
    em.persist(watchRoomParticipantForOwner);
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
      assertEquals(byWatchRoom.size(), 1);
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
      assertEquals(byWatchRoom.size(), 0);
    }

    @Test
    @DisplayName("참여자 2명 이상")
    void moreThenTwoParticipants() {
      //given
      WatchRoomParticipant watchRoomParticipantForUser = WatchRoomParticipant.builder()
          .watchRoom(watchRoom)
          .user(user)
          .createdAt(LocalDateTime.now())
          .build();
      em.persist(watchRoomParticipantForUser);

      //when
      List<WatchRoomParticipant> byWatchRoom = watchRoomParticipantRepository.findByWatchRoom(
          watchRoom);

      //then
      assertEquals(byWatchRoom.size(), 2);
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
      assertEquals(1L, total);
    }

    @Test
    @DisplayName("컨텐츠 제목에 검색어가 있음")
    void whenWatchRoomContentTitleContainsKeyword() {
      //given
      String keyword = "미";

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertEquals(1L, total);
    }

    @Test
    @DisplayName("시청방 제목, 컨텐츠 제목, 소유자 이름 모두에 키워드가 들어가지 않음")
    void whenNothingContainsKeyword() {
      //given
      String keyword = "test";

      //when
      long total = watchRoomParticipantRepository.countWatchRoomContentWithHeadcountDto(keyword);

      //then
      assertEquals(0L, total);
    }
  }

  @Nested
  @DisplayName("시청방 정보 개별 조회")
  class GetWatchRoomContentWithHeadcountDtoTest {

    private WatchRoomParticipant watchRoomParticipantForUser;

    @BeforeEach
    void setUp() {
      watchRoomParticipantForUser = WatchRoomParticipant.builder()
          .watchRoom(watchRoom)
          .user(user)
          .createdAt(LocalDateTime.now())
          .build();
      em.persist(watchRoomParticipantForUser);
    }

    @Test
    @DisplayName("조회 결과 있음")
    void success() {
      //given
      UUID watchRoomId = watchRoom.getId();

      //when
      WatchRoomContentWithParticipantCountDto result = watchRoomParticipantRepository
          .getWatchRoomContentWithHeadcountDto(watchRoomId)
          .orElse(null);

      //then
      assertNotNull(result);
      assertEquals(watchRoom, result.getWatchRoom());
      assertEquals(owner, result.getWatchRoom().getOwner());
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
    @DisplayName("참여자가 없음")
    void noOneInWatchRoom() {
      //given
      UUID watchRoomId = watchRoom.getId();

      watchRoomParticipantRepository.delete(watchRoomParticipantForOwner);
      watchRoomParticipantRepository.delete(watchRoomParticipantForUser);

      //when
      WatchRoomContentWithParticipantCountDto result = watchRoomParticipantRepository
          .getWatchRoomContentWithHeadcountDto(watchRoomId)
          .orElse(null);

      //then
      assertNotNull(result);
      assertEquals(watchRoom, result.getWatchRoom());
      assertEquals(owner, result.getWatchRoom().getOwner());
      assertEquals(content, result.getContent());
      assertEquals(0L, result.getParticipantCount().longValue());
    }
  }

  @Nested
  @DisplayName("검색 결과 페이지네이션 조회 ")
  class GetAllWatchRoomContentWithHeadcountDtoPaginatedTest {

    LocalDateTime dateCursor;
    String titleCursor;
    UUID cursorId;
    UUID participantCountCursorId;

    List<UUID> roomsWith1Participant = new ArrayList<>();
    List<UUID> roomsWith2Participants = new ArrayList<>();

    @BeforeEach
    void setUp() {
      //given
      em.createQuery("DELETE FROM WatchRoomParticipant p WHERE p.watchRoom.id = :roomId")
          .setParameter("roomId", watchRoom.getId())
          .executeUpdate();

      em.createQuery("DELETE FROM WatchRoom w WHERE w.id = :roomId")
          .setParameter("roomId", watchRoom.getId())
          .executeUpdate();

      em.clear();

      LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
      for (int i = 0; i < 10; i++) {
        LocalDateTime recordTime = baseTime.plusDays(i);
        WatchRoom watchRoomForSearchTest = WatchRoom.builder()
            .title("테스트시청방" + i)
            .owner(user)
            .content(content)
            .createdAt(recordTime)
            .build();
        em.persist(watchRoomForSearchTest);

        WatchRoomParticipant watchRoomParticipant1 = WatchRoomParticipant.builder()
            .watchRoom(watchRoomForSearchTest)
            .user(user)
            .createdAt(recordTime)
            .build();
        em.persist(watchRoomParticipant1);

        if (i == 4) {
          dateCursor = watchRoomForSearchTest.getCreatedAt();
          titleCursor = watchRoomForSearchTest.getTitle();
          cursorId = watchRoomForSearchTest.getId();
        }

        if (i > 6) {
          WatchRoomParticipant watchRoomParticipant2 = WatchRoomParticipant.builder()
              .watchRoom(watchRoomForSearchTest)
              .user(owner)
              .createdAt(recordTime) // 여기도 동일한 시간 사용
              .build();
          em.persist(watchRoomParticipant2);
          roomsWith2Participants.add(watchRoomForSearchTest.getId());
          if (i == 7) {
            participantCountCursorId = watchRoomForSearchTest.getId();
          }
        } else {
          roomsWith1Participant.add(watchRoomForSearchTest.getId());
        }
      }
    }

    @Test
    @DisplayName("시청방 제목에 검색어가 있음, Title, ASC, Cursor null, size 20, 결과는 10")
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
      assertEquals(10, result.size());
      assertEquals(1L, result.get(0).getParticipantCount().longValue());
      assertTrue(result.get(0).getWatchRoom().getOwner().equals(user));
      assertEquals("테스트시청방0", result.get(0).getWatchRoom().getTitle());
    }

////    todo - 테스트 수정
////    테스트 할때마다 결과가 달라짐
//    @Test
//    @DisplayName("시청방 소유자 이름에 검색어가 있음, participantCount, Desc, Cursor not null, size 10, 결과는 7")
//    void whenFilteringByParticipantCountCursorDesc() {
//      //given
//      String lastValue = "2";
//      String lastId = participantCountCursorId.toString();
//
//      Cursor cursor = new Cursor(lastValue, lastId);
//
//      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
//          .searchKeyword("er")
//          .sortBy("participantCount")
//          .direction("desc")
//          .cursor(cursor)
//          .size(10)
//          .build();
//
//      //when
//      List<WatchRoomContentWithParticipantCountDto> result =
//          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);
//
//      //then
//      // 참여자 수가 1인 방의 개수와 결과 개수가 일치해야 함
//      assertEquals(roomsWith1Participant.size(), result.size());
//
//    }


    @Test
    @DisplayName("시청방 소유자 이름에 검색어가 있음, Title, DESC, Cursor null, size 9, 결과는 10")
    void whenWatchRoomOwnerNameContainsKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("use")
          .sortBy("title")
          .direction("desc")
          .cursor(cursor)
          .size(9)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertEquals(10, result.size());
      assertTrue(result.get(0).getWatchRoom().getOwner().equals(user));
      assertEquals("테스트시청방9", result.get(0).getWatchRoom().getTitle());
    }

    @Test
    @DisplayName("컨텐츠 제목에 검색어가 있음, createdAt, ASC, Cursor not null, size 10, 결과는 5")
    void whenWatchRoomContentTitleContainsKeyword() {

      String lastValue = dateCursor.toString();
      String lastId = cursorId.toString();

      Cursor cursor = new Cursor(lastValue, lastId);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("미")
          .sortBy("createdAt")
          .direction("asc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertEquals(5, result.size());
      assertTrue(result.get(0).getContent().getTitle().contains("미"));
    }

    @Test
    @DisplayName("시청방 제목, 컨텐츠 제목, 소유자 이름 모두에 키워드가 들어가지 않음")
    void whenNothingContainsKeyword() {
      //given
      Cursor cursor = new Cursor(null, null);

      WatchRoomSearchInternalDto request = WatchRoomSearchInternalDto.builder()
          .searchKeyword("안녕")
          .sortBy("createdAt")
          .direction("asc")
          .cursor(cursor)
          .size(10)
          .build();

      //when
      List<WatchRoomContentWithParticipantCountDto> result =
          watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDtoPaginated(request);

      //then
      assertEquals(0, result.size());
    }
  }
}
