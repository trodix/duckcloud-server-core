package com.trodix.duckcloud.persistance.entities;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NodeContent {

    private Long nodeId;
    private String contentPath;
    private Long contentSize;
    private float contentVersion = 1.0f;
    private OffsetDateTime createdAt;
    private String createdBy;

}
