package com.trodix.duckcloud.core.presentation.dto.mappers;

import com.trodix.duckcloud.core.persistance.entities.Tag;
import com.trodix.duckcloud.core.presentation.dto.requests.TagRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.TagResponse;
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
