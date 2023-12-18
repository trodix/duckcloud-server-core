package com.trodix.duckcloud.presentation.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NodeMoveRequest {

    @NotNull
    private Long destinationId;

}
