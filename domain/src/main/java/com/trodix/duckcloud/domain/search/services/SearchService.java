package com.trodix.duckcloud.domain.search.services;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.trodix.duckcloud.domain.search.models.NodeIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @See https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.operations.queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private static final String NODE_INDEX = "node";

    private final ElasticsearchOperations elasticsearchOperations;

    public List<NodeIndex> findNodeByFieldContaining(final String field, final Serializable value, final Integer limit) {
        log.debug("Search with query [{}={}] and limit [{}]", field, value, limit);

        // 1. Create query with term conditions
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();

        Query query = MatchQuery.of(m -> m
                .field(field)
                .query((String) value)
        )._toQuery();

        nativeQueryBuilder.withQuery(query);

        if (limit > 0) {
            nativeQueryBuilder.withMaxResults(limit);
        }

        final NativeQuery searchQuery = nativeQueryBuilder.build();

        // 2. Execute search
        final SearchHits<NodeIndex> indexHits =
                elasticsearchOperations
                        .search(searchQuery, NodeIndex.class,
                                IndexCoordinates.of(NODE_INDEX));

        // 3. Map searchHits to index list
        final List<NodeIndex> indexMatches = new ArrayList<>();
        indexHits.forEach(searchHit -> indexMatches.add(searchHit.getContent()));

        log.debug("Results found: {}", indexMatches.size());

        return indexMatches;
    }

}
