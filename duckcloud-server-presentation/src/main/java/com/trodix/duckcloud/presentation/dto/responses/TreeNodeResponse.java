package com.trodix.duckcloud.presentation.dto.responses;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TreeNodeResponse {

    private Long id;

    private Long parentId;

    private Integer nodeLevel;

    private String type;

    private String name;

    private List<String> nodePath;

    private List<TagResponse> tags;

    private List<PropertyResponse> properties;

    private List<TreeNodeResponse> children = new ArrayList<>();

}
