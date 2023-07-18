package com.trodix.duckcloud.presentation.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionResponse {

    private String sub;
    private String obj;
    private String act;

}
