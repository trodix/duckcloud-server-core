package com.trodix.duckcloud.core.presentation.dto.mappers;

import com.trodix.duckcloud.core.persistance.entities.Tag;
import com.trodix.duckcloud.core.presentation.dto.requests.TagRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.TagResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    Tag toEntity(TagRequest request);

    TagResponse toDto(Tag tag);

    List<TagResponse> toDto(List<Tag> tags);

}
