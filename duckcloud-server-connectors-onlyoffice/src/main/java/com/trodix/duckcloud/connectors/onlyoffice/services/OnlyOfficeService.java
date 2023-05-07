package com.trodix.duckcloud.connectors.onlyoffice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.trodix.duckcloud.connectors.onlyoffice.dto.requests.OnlyOfficeDocumentType;
import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.domain.models.FileLocationParts;
import com.trodix.duckcloud.domain.models.FileStoreMetadata;
import com.trodix.duckcloud.domain.services.NodeService;
import com.trodix.duckcloud.domain.utils.StorageUtils;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import com.trodix.duckcloud.security.services.AuthenticationService;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OnlyOfficeService {

    private final NodeService nodeService;

    private final AuthenticationService authenticationService;

    @Value("${app.onlyoffice.jwt.secret}")
    private String onlyOfficeJwtSecret;

    @Value("${app.server.public-base-url}")
    private String serverPublicBaseUrl;

    public String createJwtToken(JsonObject payloadClaims) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> payload = mapper.readValue(payloadClaims.toString(), new TypeReference<>() {
            });

            Signer signer = HMACSigner.newSHA256Signer(onlyOfficeJwtSecret);
            JWT jwt = new JWT();
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                jwt.addClaim(entry.getKey(), entry.getValue());
            }
            return JWT.getEncoder().encode(jwt, signer);
        } catch (Exception e) {
            return "";
        }
    }

    public JsonObject generateEditorConfig(Long nodeId) {

        Node node = nodeService.getOne(nodeId).orElseThrow(() -> new IllegalArgumentException("nodeId " + nodeId + " not found"));

        String filename = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_NAME).orElseThrow().getStringVal();
        String fileExt = FilenameUtils.getExtension(filename);
        String key = "node_" + node.getId() + "_1.0";
        String documentType = getOnlyOfficeDocumentType(fileExt).toString().toLowerCase();
        String downloadUrl = serverPublicBaseUrl + "/api/v1/integration/onlyoffice/document/" + nodeId + "/contents";
        String callbackUrl = serverPublicBaseUrl + "/api/v1/integration/onlyoffice/document";
        String lang = System.getProperty("user.language") + "_" + System.getProperty("user.country");

        JsonObject config = Json.createObjectBuilder()
                .add("document", Json.createObjectBuilder()
                        .add("title", filename)
                        .add("url", downloadUrl)
                        .add("fileType", fileExt)
                        .add("key", key)
                        .add("permissions", Json.createObjectBuilder()
                                .add("edit", true)
                                .add("download", true)
                                .add("fillForms", true)
                                .add("copy", true)
                                .add("comment", true)))
                .add("documentType", documentType)
                .add("editorConfig", Json.createObjectBuilder()
                        .add("callbackUrl", callbackUrl)
                        .add("lang", lang)
                        .add("user", Json.createObjectBuilder()
                                .add("id", authenticationService.getUserId())
                                .add("name", authenticationService.getName())))
                .build();

        String token = createJwtToken(config);
        config = Json.createObjectBuilder(config)
                .add("token", token)
                .build();

        return config;
    }

    public void updateDocument(String nodeId, double version, String url) {
        log.debug("Updating document (nodeId={} version={}) from url: {}", nodeId, version, url);

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

    private OnlyOfficeDocumentType getOnlyOfficeDocumentType(String extension) {

        if (OnlyOfficeDocumentType.getSupportByWordExtensions().contains(extension)) {
            return OnlyOfficeDocumentType.WORD;
        } else if (OnlyOfficeDocumentType.getSupportByCellExtensions().contains(extension)) {
            return OnlyOfficeDocumentType.CELL;
        } else if (OnlyOfficeDocumentType.getSupportBySlideExtensions().contains(extension)) {
            return OnlyOfficeDocumentType.SLIDE;
        }

        throw new IllegalArgumentException("file extension " + extension + " not supported");
    }
}
