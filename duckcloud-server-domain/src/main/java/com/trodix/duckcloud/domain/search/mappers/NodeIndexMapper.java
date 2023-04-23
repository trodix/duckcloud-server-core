package com.trodix.duckcloud.domain.search.mappers;

import com.trodix.duckcloud.domain.search.models.NodeIndex;
import com.trodix.duckcloud.persistance.entities.Node;
import org.apache.commons.lang.NotImplementedException;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface NodeIndexMapper {

    default Node toNode(NodeIndex nodeIndex) {
        throw new NotImplementedException();
    }

    List<Node> toNode(List<NodeIndex> nodeIndex);

}
