package team03.mopl.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = {
        "team03.mopl.domain.user",
        "team03.mopl.domain.content.repository", // JPA Repository들
        "team03.mopl.domain.curation.repository",
        "team03.mopl.domain.review.repository",
        "team03.mopl.domain.dm.repository",
        "team03.mopl.domain.follow.repository",
        "team03.mopl.domain.notification.repository",
        "team03.mopl.domain.playlist.repository",
        "team03.mopl.domain.subscription",
        "team03.mopl.domain.user",
        "team03.mopl.domain.watchroom.repository",
        "team03.mopl.jwt"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = org.springframework.data.elasticsearch.repository.ElasticsearchRepository.class
    )
)
//@EnableElasticsearchRepositories(
//    basePackages = {
//        "team03.mopl.domain.curation.elasticsearch" // Elasticsearch Repository들만
//    }
//)
public class RepositoryConfig {
  // Repository 스캔 범위를 명확하게 분리
}
