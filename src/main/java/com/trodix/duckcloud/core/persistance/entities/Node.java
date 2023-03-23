package com.trodix.duckcloud.core.persistance.entities;

import lombok.Data;

@Data
public class Node {

    private Long id;

    private Long parentId;

    private Long typeId;

}
