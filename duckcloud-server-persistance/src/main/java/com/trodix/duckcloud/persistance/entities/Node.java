package com.trodix.duckcloud.persistance.entities;

import com.trodix.duckcloud.security.annotations.FilterResourceId;
import lombok.Data;

import java.util.List;

@Data
public class Node {

    @FilterResourceId
    private Long id;

    private Long parentId;

    private Type type;

    private List<Tag> tags;

    private List<Property> properties;

}
