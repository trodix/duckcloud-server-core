package com.trodix.duckcloud.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodePath {

    private Long nodeId;

    private String nodeName;

}
