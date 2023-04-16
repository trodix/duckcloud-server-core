package com.trodix.duckcloud.presentation.dto.responses;

import com.trodix.duckcloud.domain.models.NodePath;
import lombok.Data;

import java.util.List;

@Data
public class NodeResponse {

    private Long id;

    private Long parentId;

    private List<NodePath> path;

    private String type;

    private List<String> tags;

    private List<PropertyResponse> properties;

}
