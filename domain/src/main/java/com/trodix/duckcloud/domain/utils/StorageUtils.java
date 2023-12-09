package com.trodix.duckcloud.domain.utils;

import com.trodix.duckcloud.domain.models.FileLocationParts;

public class StorageUtils {

    public static FileLocationParts getFileLocationParts(String contentLocation) {
        String[] parts = contentLocation.split(":");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid content location format. expected format: '<bucket-name>:<path>'");
        }

        FileLocationParts locationParts = FileLocationParts
                .builder()
                .bucket(parts[0])
                .path(parts[1])
                .build();

        return locationParts;
    }

}
