package com.trodix.duckcloud.core.presentation.dto.mappers;

import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.NodeResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {TypeMapper.class, TagMapper.class, PropertyMapper.class})
public interface NodeMapper  {

    Node toEntity(NodeRequest request);

    NodeResponse toDto(Node node);

    List<Node> toEntity(List<NodeRequest> request);

    List<NodeResponse> toDto(List<Node> node);

}
