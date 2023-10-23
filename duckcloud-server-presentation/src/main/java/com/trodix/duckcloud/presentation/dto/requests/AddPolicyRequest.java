package com.trodix.duckcloud.presentation.dto.requests;

import lombok.Data;

@Data
public class AddPolicyRequest {

    private String sub;
    private String obj;
    private String act;

}
