package com.yuliyuli.repository;

import com.yuliyuli.document.VideoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<VideoDocument, String> {
  @Query(
      "{"
          + "  \"match\": {"
          + "    \"title\": {"
          + "      \"query\": \"?0\", "
          + "      \"operator\": \"and\""
          + "    }"
          + "  }"
          + "}")
  Page<VideoDocument> findByTitleSuggest(String title, Pageable pageable);
}
