package com.trodix.duckcloud.core.presentation.dto.responses;

import com.trodix.duckcloud.core.persistance.entities.Node;
import lombok.Data;

import java.util.List;

@Data
public class NodeResponse {

    private Long id;

    private Long parentId;

    private TypeResponse type;

    private List<TagResponse> tags;

    private List<PropertyResponse> properties;

}
