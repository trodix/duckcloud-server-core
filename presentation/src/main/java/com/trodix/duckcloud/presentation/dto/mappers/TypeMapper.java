package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.persistance.entities.Type;
import org.mapstruct.Mapper;

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
