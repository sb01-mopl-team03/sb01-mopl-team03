package team03.mopl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import team03.mopl.domain.curation.service.ContentSearchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationInitializer implements ApplicationRunner {

  private final ContentSearchService contentSearchService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("애플리케이션 시작 후 OpenSearch 인덱스 초기화 및 콘텐츠 재인덱싱 실행.");
    try {
      contentSearchService.initializeIndexWithAllContents(); // 이 메서드 호출
    } catch (Exception e) {
      log.error("애플리케이션 시작 시 OpenSearch 인덱스 초기화 및 재인덱싱 실패", e);
      // 필요에 따라 애플리케이션 종료 또는 경고 처리
    }
  }
}
