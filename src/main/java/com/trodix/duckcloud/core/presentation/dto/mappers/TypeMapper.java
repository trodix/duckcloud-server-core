package com.trodix.duckcloud.core.presentation.dto.mappers;

import org.mapstruct.Mapper;
import com.trodix.duckcloud.core.persistance.entities.Type;
import com.trodix.duckcloud.core.presentation.dto.requests.TypeRequest;

@Mapper
public interface TypeMapper {

    Type toEntity(TypeRequest request);

}
