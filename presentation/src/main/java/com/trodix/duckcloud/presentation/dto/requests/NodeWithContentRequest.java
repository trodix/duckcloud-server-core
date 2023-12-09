package com.trodix.duckcloud.presentation.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NodeWithContentRequest {

    private Long id;

    private Long parentId;

    @NotBlank
    private String type;

    private List<String> tags = new ArrayList<>();

    private Map<String, Serializable> properties = new HashMap<>();

}
