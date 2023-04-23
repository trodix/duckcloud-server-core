package com.trodix.duckcloud.domain.search.services;

import com.trodix.duckcloud.domain.search.models.NodeIndex;
import com.trodix.duckcloud.domain.services.NodeContentService;
import com.trodix.duckcloud.domain.utils.ModelUtils;
import com.trodix.duckcloud.persistance.dao.NodeManager;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeIndexerService {

    private static final String NODE_INDEX = "node";

    private final ElasticsearchOperations elasticsearchOperations;

    private final NodeManager nodeManager;

    private final NodeContentService nodeContentService;

    @Value("${app.indexes.file-content-indexer.enabled}")
    private boolean isFileContentIndexerEnabled;

    @Async
    @Scheduled(fixedDelayString = "${app.indexes.synchronization.fixed-delay}", timeUnit = TimeUnit.MINUTES)
    public List<Future<?>> synchronizeIndexes() {
        log.info("Starting to synchronize indexes");

        final int MAX_THREAD_NUMBER = 4;
        final int BATCH_SIZE = 100;
        final long count = nodeManager.count();
        final long pageCount = (count > 0 && count >= BATCH_SIZE)
                ? (count / BATCH_SIZE)
                : 1;

        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        final List<Future<?>> runningTasks = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            final List<Node> page = nodeManager.findAll(0, 100);

            final List<NodeIndex> nodeIndexChunk = page.stream().map(this::buildIndex).toList();

            final Runnable task = () -> createNodeIndexBulk(nodeIndexChunk);
            log.debug("Adding new task to thread pool with page {}/{} ({} records)", pageIndex + 1, pageCount, count);
            final Future<?> pendingTask = executor.submit(task);
            runningTasks.add(pendingTask);
        }

        return runningTasks;
    }

    public List<IndexedObjectInformation> createNodeIndexBulk(final List<NodeIndex> nodes) {

        final List<IndexQuery> queries = nodes.stream()
                .map(node -> new IndexQueryBuilder()
                        .withId(node.getDbId().toString())
                        .withObject(node).build())
                .collect(Collectors.toList());

        log.debug("Running Bulk index query for {} items", nodes.size());

        final List<IndexedObjectInformation> result = elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(NODE_INDEX));
        log.debug("{} items indexed from bulk query", result.size());

        return result;
    }

    public String createNodeIndex(final NodeIndex node) {

        if (node.getDbId() == null) {
            throw new IllegalArgumentException("node.getDbId() was null");
        }

        final IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(node.getDbId().toString())
                .withObject(node).build();

        final String documentId = elasticsearchOperations
                .index(
                        indexQuery,
                        IndexCoordinates.of(NODE_INDEX));

        log.debug("New index created for nodeId {}", node.getDbId());

        return documentId;
    }

    public void deleteNodeIndex(final Long nodeId) {

        final IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(nodeId.toString())
                .build();

        elasticsearchOperations.delete(indexQuery, IndexCoordinates.of(NODE_INDEX));
    }

    public NodeIndex buildIndex(Node node) {
        NodeIndex nodeIndex = new NodeIndex();

        nodeIndex.setDbId(node.getId());
        nodeIndex.setType(node.getType().getName());
        nodeIndex.setTags(NodeUtils.tagsToNameList(node.getTags()));
        nodeIndex.setProperties(NodeUtils.toMapProperties(node.getProperties()));

        if (isFileContentIndexerEnabled) {
            if (ModelUtils.isContentType(node)) {
                String fileContent = nodeContentService.extractFileTextContent(node);
                nodeIndex.setFilecontent(fileContent);
            } else {
                log.trace("Not indexing nodeId {} because node is not of type content", node.getId());
            }
        } else {
            log.trace("Not indexing text file content because the option is disabled");
        }

        return nodeIndex;
    }

}
