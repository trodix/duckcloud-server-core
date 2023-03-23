package com.trodix.duckcloud.core.presentation.dto.responses;

import lombok.Data;

import java.io.Serializable;

@Data
public class PropertyResponse {

    private String key;

    private Serializable value;

}
