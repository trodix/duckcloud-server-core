package com.trodix.duckcloud.domain.models;

import lombok.Data;

@Data
public class FileStoreMetadata {

    private String uuid;

    private String bucket;

    private String directoryPath;

    private String contentType;

    private String originalName;

}
