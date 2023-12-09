package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.persistance.entities.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    default Tag toEntity(String request) {
        Tag tag = new Tag();
        tag.setName(request);

        return tag;
    }

    default String toDto(Tag tag) {
        return tag.getName();
    }

    List<String> toDto(List<Tag> tags);

}
