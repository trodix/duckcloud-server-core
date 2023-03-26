package com.trodix.duckcloud.presentation.dto.responses;

import lombok.Data;

import java.util.List;

@Data
public class NodeResponse {

    private Long id;

    private Long parentId;

    private String type;

    private List<String> tags;

    private List<PropertyResponse> properties;

}
