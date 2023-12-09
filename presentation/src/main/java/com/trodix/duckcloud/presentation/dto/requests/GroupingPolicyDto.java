package com.trodix.duckcloud.presentation.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupingPolicyDto {

    private String group;
    private String role;

}
