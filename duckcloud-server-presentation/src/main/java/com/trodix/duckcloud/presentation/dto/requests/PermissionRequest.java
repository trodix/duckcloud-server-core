package com.trodix.duckcloud.presentation.dto.requests;

import lombok.Data;

@Data
public class PermissionRequest {

    private String sub;
    private String obj;
    private String act;

}
