package com.trodix.duckcloud.presentation.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolicyDto {

    private String sub;
    private String obj;
    private String act;

}
