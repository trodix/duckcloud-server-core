package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.domain.models.NodePath;
import org.mapstruct.Mapper;

@Mapper
public interface NodePathMapper {

    NodePath toDto(NodePath path);

}
