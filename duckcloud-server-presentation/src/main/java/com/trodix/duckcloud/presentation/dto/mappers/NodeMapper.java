package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.domain.models.NodeWithPath;
import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import com.trodix.duckcloud.presentation.dto.requests.NodeWithContentRequest;
import com.trodix.duckcloud.presentation.dto.responses.NodeResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {TypeMapper.class, TagMapper.class, PropertyMapper.class, NodePathMapper.class})
public interface NodeMapper  {

    Node toEntity(NodeRequest request);

    Node toEntity(NodeWithContentRequest request);

    NodeResponse toDto(Node node);

    List<Node> toEntity(List<NodeRequest> request);

    List<NodeResponse> toDto(List<Node> node);

    List<NodeResponse> toDto2(List<NodeWithPath> node);

    NodeResponse toDto2(NodeWithPath node);

}
