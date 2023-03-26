package com.trodix.duckcloud.core.presentation.controllers;

import com.trodix.duckcloud.core.domain.models.FileStoreMetadata;
import com.trodix.duckcloud.core.domain.services.NodeService;
import com.trodix.duckcloud.core.domain.services.StorageService;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.presentation.dto.mappers.NodeMapper;
import com.trodix.duckcloud.core.presentation.dto.requests.NodeRequest;
import io.minio.messages.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Scope(WebApplicationContext.SCOPE_REQUEST)
@RequestMapping("/api/v1/storage")
@AllArgsConstructor
@Slf4j
@RolesAllowed({"ecm-user"})
public class StorageController {

    private final StorageService storageService;

    private final NodeService nodeService;

    private final NodeMapper nodeMapper;


    @Operation(summary = "Get the list of available buckets where at least one file is stored")
    @GetMapping(path = "/buckets")
    public List<Bucket> listBuckets() {
        return storageService.listBuckets();
    }

    @Operation(summary = "Create a new node and attach a file")
    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void create(@Valid NodeRequest request, @RequestPart(value = "file") final MultipartFile file) throws IOException {

        final Node node = nodeMapper.toEntity(request);
        FileStoreMetadata fileStoreMetadata = nodeService.buildFileStoreMetadata(node, file);
        nodeService.createNodeWithContent(node, fileStoreMetadata, file.getBytes());

    }

}
