package com.trodix.duckcloud.persistance.entities;

import lombok.Data;

import java.util.List;

@Data
public class Node {

    private Long id;

    private Long parentId;

    private Type type;

    private List<Tag> tags;

    private List<Property> properties;

}
