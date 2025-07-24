package team03.mopl.domain.curation.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<ContentSearch, String> {

}
