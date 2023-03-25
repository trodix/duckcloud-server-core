package com.trodix.duckcloud.core.presentation.dto.mappers;

import org.mapstruct.Mapper;
import com.trodix.duckcloud.core.persistance.entities.Type;
import com.trodix.duckcloud.core.presentation.dto.requests.TypeRequest;
import org.mapstruct.Mapping;

@Mapper
public interface TypeMapper {

    default Type toEntity(String request) {
        Type type = new Type();
        type.setName(request);

        return type;
    }

    default String toDto(Type type) {
        return type.getName();
    }

}
