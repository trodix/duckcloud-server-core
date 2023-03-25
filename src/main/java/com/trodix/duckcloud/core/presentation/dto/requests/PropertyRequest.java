package com.trodix.duckcloud.core.presentation.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class PropertyRequest {

    @NotBlank
    private String key;

    @NotBlank
    private Serializable value;

}