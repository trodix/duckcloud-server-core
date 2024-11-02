package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.domain.exceptions.ParsingContentException;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.models.FileLocationParts;
import com.trodix.duckcloud.domain.search.services.FileSearchService;
import com.trodix.duckcloud.domain.utils.ModelUtils;
import com.trodix.duckcloud.domain.utils.StorageUtils;
import com.trodix.duckcloud.persistance.dao.mappers.NodeContentMapper;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.entities.NodeContent;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeContentService {

    private static final float DEFAULT_VERSION = 1.0f;
    private static final float MINOR_VERSION_INC = 0.1f;
    private static final float MAJOR_VERSION_INC = 1.0f;

    private final NodeContentMapper nodeContentMapper;

    private final StorageService storageService;

    private final FileSearchService fileSearchService;

    private float getNodeGreaterVersion(Long nodeId) {
        List<NodeContent> nodeContentList = findByNodeId(nodeId);
        return nodeContentList.stream().map(NodeContent::getContentVersion).max(Comparator.naturalOrder()).orElse(0f);
    }

    public float getNextMinorVersion(Node node) {
        float nodeCurrentVersion = getNodeGreaterVersion(node.getId());
        return nodeCurrentVersion >= DEFAULT_VERSION ? nodeCurrentVersion + MINOR_VERSION_INC : DEFAULT_VERSION;
    }

    public float getNextMajorVersion(Node node) {
        float nodeCurrentVersion = getNodeGreaterVersion(node.getId());
        return nodeCurrentVersion >= DEFAULT_VERSION ? nodeCurrentVersion + MAJOR_VERSION_INC : DEFAULT_VERSION;
    }

    public List<NodeContent> findByNodeId(Long nodeId) {
        return nodeContentMapper.findByNodeId(nodeId);
    }

    public Optional<NodeContent> findByNodeIdAndVersion(Long nodeId, float contentVersion) {
        return nodeContentMapper.findByNodeIdAndVersion(nodeId, contentVersion);
    }

    public void insert(NodeContent nodeContent) {
        nodeContentMapper.insert(nodeContent);
    }

    public void deleteAllByNodeId(Long nodeId) {
        nodeContentMapper.deleteAllByNodeId(nodeId);
    }

    public void deleteByNodeIdAndVersion(Long nodeId, Integer contentVersion) {
        nodeContentMapper.deleteByNodeIdAndVersion(nodeId, contentVersion);
    }

    public String extractFileTextContent(Node node) throws ParsingContentException {

        if (!ModelUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node must be of type " + ContentModel.TYPE_CONTENT + " to extract file content");
        }

        String contentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION)
                .orElseThrow(() -> new IllegalStateException("Content location not found for nodeId " + node.getId()))
                .getStringVal();

        FileLocationParts parts = StorageUtils.getFileLocationParts(contentLocation);

        byte[] file = storageService.getFile(parts.getBucket(), parts.getPath());

        return fileSearchService.extractFileTextContent(file);
    }

}
