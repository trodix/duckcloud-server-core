package com.trodix.duckcloud.presentation.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtendedPermissionResponse {

    private PermissionResponse permission;
    private boolean hasPermission;

}
