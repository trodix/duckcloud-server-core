package com.trodix.duckcloud.core.presentation.dto.mappers;

import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.presentation.dto.requests.NodeRequest;
import org.mapstruct.Mapper;

@Mapper
public interface NodeMapper {

    Node toEntity(NodeRequest request);

}
