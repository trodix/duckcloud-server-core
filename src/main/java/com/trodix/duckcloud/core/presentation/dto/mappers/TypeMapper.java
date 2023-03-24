package com.trodix.duckcloud.core.presentation.dto.mappers;

import org.mapstruct.Mapper;
import com.trodix.duckcloud.core.persistance.entities.Type;
import com.trodix.duckcloud.core.presentation.dto.requests.TypeRequest;
import org.mapstruct.Mapping;

@Mapper
public interface TypeMapper {

    Type toEntity(TypeRequest request);

    default String toDto(Type type) {
        return type.getName();
    }

}
