package com.trodix.duckcloud.connectors.onlyoffice.controllers;

import com.trodix.duckcloud.connectors.onlyoffice.dto.requests.OnlyOfficeCallbackRequest;
import com.trodix.duckcloud.connectors.onlyoffice.dto.responses.OnlyOfficeUpdatedDocumentResponse;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.models.FileLocationParts;
import com.trodix.duckcloud.domain.models.FileStoreMetadata;
import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.domain.services.StorageService;
import com.trodix.duckcloud.domain.utils.StorageUtils;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.entities.Property;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/integration/onlyoffice")
public class OnlyOfficeIntegrationController {

    private final NodeService nodeService;

    @Operation(summary = "Get the file content")
    @GetMapping("/document/{nodeId}/contents")
    public ResponseEntity<ByteArrayResource> getDocumentContentByNodeId(@PathVariable final Long nodeId) {
        final Optional<Node> nodeOptional = nodeService.getOne(nodeId);

        if (nodeOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Node not found for nodeId " + nodeId);
        }

        Node node = nodeOptional.get();

        final byte[] data;

        try {
            data = nodeService.getFileContent(node);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filecontent not found for nodeId " + nodeId);
        }

        final ByteArrayResource resource = new ByteArrayResource(data);

        final Optional<Property> nameProp = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_NAME);

        if (nameProp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property: " + ContentModel.PROP_NAME + " not found for nodeId " + nodeId);
        }

        final String filename = nameProp.get().getStringVal();

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Operation(summary = "Update the file content", description = "See https://api.onlyoffice.com/editors/callback")
    @PostMapping(path = "/document", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public OnlyOfficeUpdatedDocumentResponse updateDocumentContent(@Valid @RequestBody final OnlyOfficeCallbackRequest data, HttpServletRequest req) {

        log.trace("Header Authentication: " + req.getHeader("Authorization"));
        log.debug("Document data received from OnlyOffice: \n" + data);

        switch (data.getStatus()) {
            case READY_FOR_SAVING:
            case SAVING_ERROR:
            case DOCUMENT_EDITED_STATE_SAVED:
            case FORCE_SAVING_ERROR:
                updateDocument(data.getKey(), data.getUrl());
                break;
            default:
                break;
        }

        return new OnlyOfficeUpdatedDocumentResponse(0);
    }

    private void updateDocument(String nodeId, String url) {
        log.debug("Updating document (nodeId={}) from url: {}", nodeId, url);

        if (url == null) {
            return;
        }

        Node node = nodeService.getOne(Long.valueOf(nodeId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Node not found for nodeId " + nodeId));
        String contentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property: " + ContentModel.PROP_CONTENT_LOCATION + " not found for nodeId " + nodeId)).getStringVal();
        FileLocationParts parts = StorageUtils.getFileLocationParts(contentLocation);
        String documentId;

        try {
            documentId = parts.getDocumentUUID();
        } catch (IllegalArgumentException e) {
            String msg = "Property: " + ContentModel.PROP_CONTENT_LOCATION + " is invalid for nodeId " + nodeId;
            log.error(msg, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        try (InputStream is = new URL(url).openConnection().getInputStream()) {

            FileStoreMetadata fileStoreMetadata = new FileStoreMetadata();
            fileStoreMetadata.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"); // FIXME
            fileStoreMetadata.setUuid(documentId);
            fileStoreMetadata.setBucket(parts.getBucket());

            nodeService.updateNodeContent(node, fileStoreMetadata, is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
