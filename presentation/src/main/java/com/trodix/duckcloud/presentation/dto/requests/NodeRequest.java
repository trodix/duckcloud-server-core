package com.trodix.duckcloud.presentation.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeRequest {

    private Long id;

    private Long parentId;

    @NotBlank
    private String type;

    private List<String> tags = new ArrayList<>();

    private List<PropertyRequest> properties;

}
