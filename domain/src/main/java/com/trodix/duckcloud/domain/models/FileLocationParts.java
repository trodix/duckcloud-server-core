package com.trodix.duckcloud.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class FileLocationParts {

    private String bucket;

    private String path;

    public String getDocumentUUID() throws IllegalArgumentException {

        try {
            String[] parts = getPath().split("/");
            String uuid = parts[parts.length - 1];
            UUID.fromString(uuid);
            return uuid;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public String getDirectoryPath() throws IllegalArgumentException {

        try {
            String[] parts = getPath().split("/");
            String[] directoryPathParts = Arrays.copyOf(parts, parts.length - 1);
            return StringUtils.join(directoryPathParts, '/');
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }

    }

}
