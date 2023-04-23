package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.domain.exceptions.ParsingContentException;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.models.FileLocationParts;
import com.trodix.duckcloud.domain.search.services.FileSearchService;
import com.trodix.duckcloud.domain.utils.ModelUtils;
import com.trodix.duckcloud.domain.utils.StorageUtils;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeContentService {

    private final StorageService storageService;

    private final FileSearchService fileSearchService;

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
