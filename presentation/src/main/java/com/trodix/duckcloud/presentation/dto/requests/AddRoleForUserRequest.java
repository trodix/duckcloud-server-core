package com.trodix.duckcloud.presentation.dto.requests;

import lombok.Data;

@Data
public class AddRoleForUserRequest {
    private String user;
    private String role;
}
