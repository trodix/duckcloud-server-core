package com.trodix.duckcloud.core.presentation.dto.requests;

import com.trodix.duckcloud.core.presentation.dto.responses.PropertyResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
public class NodeRequest {

    private Long id;

    private Long parentId;

    @NotBlank
    private String type;

    private List<String> tags;

    private List<PropertyRequest> properties;

}
