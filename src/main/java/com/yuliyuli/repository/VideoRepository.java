package com.yuliyuli.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.yuliyuli.document.VideoDocument;

public interface VideoRepository extends ElasticsearchRepository<VideoDocument, String> {
    @Query("{" +
           "  \"match\": {" +
           "    \"title.suggest\": {" +
           "      \"query\": \"?0\", " +
           "      \"operator\": \"and\"" +
           "    }" +
           "  }" +
           "}")
    Page<VideoDocument> findByTitleSuggest(String title, Pageable pageable);
}
