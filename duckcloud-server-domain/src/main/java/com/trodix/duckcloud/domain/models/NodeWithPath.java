package com.trodix.duckcloud.domain.models;

import com.trodix.duckcloud.persistance.entities.Property;
import com.trodix.duckcloud.persistance.entities.Tag;
import com.trodix.duckcloud.persistance.entities.Type;
import lombok.Data;

import java.util.List;

@Data
public class NodeWithPath {

    private Long id;

    private Long parentId;

    private List<NodePath> path;

    private Type type;

    private List<Tag> tags;

    private List<Property> properties;

}
