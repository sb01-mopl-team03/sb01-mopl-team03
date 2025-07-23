package team03.mopl.domain.dm.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DmRepositoryCustom.class, DmRepositoryCustomTest.QueryDslTestConfig.class})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never", // schema.sql 자동 실행 막음
    "spring.jpa.hibernate.ddl-auto=create-drop" // 내장 DB에 테이블을 자동으로 생성/삭제
})
public class DmRepositoryCustomTest {

  @Autowired
  EntityManager em;

  @Autowired
  DmRepositoryCustom dmRepositoryCustom;

  private final UUID senderId = UUID.randomUUID();
  private final UUID receiverId = UUID.randomUUID();

  private DmRoom room;

  @BeforeEach
  void setup() {
    room = new DmRoom(senderId, receiverId);
    //ReflectionTestUtils.setField(room, "id", roomId);
    ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now());
    em.persist(room);

    for (int i = 0; i < 10; i++) {
      Dm dm = new Dm(senderId, "message " + i);
      dm.setDmRoom(room);
      //ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(dm, "createdAt", LocalDateTime.now().minusMinutes(i));
      em.persist(dm);
    }

    em.flush();
    em.clear();
  }

  @Test
  void findByCursor_noCursor_shouldReturnLatest() {
    UUID actualRoomId = room.getId();
    List<Dm> result = dmRepositoryCustom.findByCursor(actualRoomId, 5, null, null);
    assertThat(result).hasSize(5);
  }

  @Test
  void findByCursor_shouldReturnNextPage() {
    UUID actualRoomId = room.getId();
    List<Dm> firstPage = dmRepositoryCustom.findByCursor(actualRoomId, 5, null, null);
    Dm last = firstPage.get(4);

    List<Dm> secondPage = dmRepositoryCustom.findByCursor(
        actualRoomId,
        5,
        last.getCreatedAt().toString(),
        last.getId().toString()
    );

    assertThat(secondPage).doesNotContain(last);
  }

  @Test
  void findByCursor_withCursor() {
    // Step 1: 첫 페이지 조회
    UUID actualRoomId = room.getId();
    List<Dm> firstPage = dmRepositoryCustom.findByCursor(actualRoomId, 5, null, null);

    assertThat(firstPage).hasSize(5);

    // Step 2: 커서 값 준비 (5번째 메시지 기준)
    Dm lastOfFirstPage = firstPage.get(4); // 0~4, 가장 마지막
    String mainCursor = lastOfFirstPage.getCreatedAt().toString();
    String subCursor = lastOfFirstPage.getId().toString();

    // Step 3: 커서 기반 다음 페이지 조회
    List<Dm> secondPage = dmRepositoryCustom.findByCursor(actualRoomId, 5, mainCursor, subCursor);

    // 검증
    assertThat(secondPage).hasSize(5); // 총 10개 넣었으므로 5개 더 있음
    assertThat(secondPage).doesNotContain(lastOfFirstPage); // 중복 없음
    assertThat(secondPage).allSatisfy(dm -> {
      boolean isOlder = dm.getCreatedAt().isBefore(lastOfFirstPage.getCreatedAt());
      boolean isSameTimeAndSmallerId = dm.getCreatedAt().isEqual(lastOfFirstPage.getCreatedAt()) &&
          dm.getId().compareTo(lastOfFirstPage.getId()) < 0;
      assertThat(isOlder || isSameTimeAndSmallerId).isTrue();
    });
  }



  @TestConfiguration
  static class QueryDslTestConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }
}

