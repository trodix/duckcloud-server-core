package com.trodix.duckcloud.connectors.onlyoffice.controllers;

import com.trodix.duckcloud.connectors.onlyoffice.dto.requests.OnlyOfficeCallbackRequest;
import com.trodix.duckcloud.connectors.onlyoffice.dto.responses.OnlyOfficeUpdatedDocumentResponse;
import com.trodix.duckcloud.connectors.onlyoffice.services.OnlyOfficeService;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.entities.Property;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/integration/onlyoffice")
public class OnlyOfficeIntegrationController {

    private final NodeService nodeService;

    private final OnlyOfficeService onlyOfficeService;

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

        log.debug("Document data received from OnlyOffice: \n" + data);

        switch (data.getStatus()) {
            case READY_FOR_SAVING:
            case SAVING_ERROR:
            case DOCUMENT_EDITED_STATE_SAVED:
            case FORCE_SAVING_ERROR:
                if (data.getUrl() == null) {
                    log.trace("Received event from OnlyOffice without document url");
                    break;
                }
                String[] keyParts = data.getKey().split("_");
                String nodeId = keyParts[0];
                String revision = keyParts[1];
                onlyOfficeService.updateDocument(nodeId, revision, data.getUrl());
                break;
            default:
                break;
        }

        return new OnlyOfficeUpdatedDocumentResponse(0);
    }

    @Operation(summary = "Generate a OnlyOffice config containing a JWT for the user in order to granting access to OnlyOffice Document Server",
            description = """
                See :
                    https://api.onlyoffice.com/editors/signature/#java
                    https://api.onlyoffice.com/editors/signature/browser
            """
    )
    @PostMapping(path = "/open-document-request/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOpenOnlyOfficeDocumentRequestConfig(@PathVariable final Long nodeId) {
        return onlyOfficeService.generateEditorConfig(nodeId).toString();
    }

}
