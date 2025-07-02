package team03.mopl.domain.chat.performance;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import team03.mopl.common.config.QueryDslConfig;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.service.ChatRoomService;
import team03.mopl.domain.chat.service.ChatRoomServiceImpl;

@DataJpaTest
@Import({QueryDslConfig.class, ChatRoomServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class ChatRoomPerformanceTest {

  @Autowired
  private ChatRoomService chatRoomService;

  @Test
  @DisplayName("N+1 vs QueryDSL 성능 비교 테스트")
  void compareQueryPerformance() {
    //given - JVM 워밍업
    chatRoomService.getAllWithN1();
    chatRoomService.getAll(); // QueryDsl 사용

    //when & then - N+1 방식
    long start1 = System.currentTimeMillis();
    List<ChatRoomDto> result1 = chatRoomService.getAllWithN1();
    long time1 = System.currentTimeMillis() - start1;

    // when & then - QueryDSL 방식
    long start2 = System.currentTimeMillis();
    List<ChatRoomDto> result2 = chatRoomService.getAll();
    long time2 = System.currentTimeMillis() - start2;

    System.out.println("=== 성능 테스트 결과 ===");
    System.out.println("N+1 방식: " + time1 + "ms (" + result1.size() + "개 결과)");
    System.out.println("QueryDSL 방식: " + time2 + "ms (" + result2.size() + "개 결과)");
    System.out.println("성능 개선: " + (time1 - time2) + "ms (" +
        (time2 > 0 ? String.format("%.1f배", (double)time1/time2) : "무한대") + " 빠름)");

    assertThat(result1).isNotEmpty();
    assertThat(result2).isNotEmpty();
    assertThat(result1.size()).isEqualTo(result2.size());
  }

  @Test
  @DisplayName("대량 데이터 성능 테스트")
  void performanceTestWithLargeData() {
    // 여러 번 실행해서 평균 측정
    int iterations = 5;
    long totalN1Time = 0;
    long totalQueryDslTime = 0;

    for (int i = 0; i < iterations; i++) {
      long start1 = System.currentTimeMillis();
      chatRoomService.getAllWithN1();
      totalN1Time += System.currentTimeMillis() - start1;

      long start2 = System.currentTimeMillis();
      chatRoomService.getAll();
      totalQueryDslTime += System.currentTimeMillis() - start2;
    }

    long avgN1Time = totalN1Time / iterations;
    long avgQueryDslTime = totalQueryDslTime / iterations;

    System.out.println("=== " + iterations + "회 평균 성능 ===");
    System.out.println("N+1 평균: " + avgN1Time + "ms");
    System.out.println("QueryDSL 평균: " + avgQueryDslTime + "ms");
    System.out.println("평균 개선: " + (avgN1Time - avgQueryDslTime) + "ms");

    if (avgQueryDslTime > 0) {
      System.out.println("평균 배수 개선: " + String.format("%.1f배", (double)avgN1Time/avgQueryDslTime));
    }
  }
}