package com.trodix.duckcloud.core.presentation.controllers;

import com.trodix.duckcloud.core.business.services.TagService;
import com.trodix.duckcloud.core.persistance.entities.Tag;
import com.trodix.duckcloud.core.presentation.dto.mappers.TagMapper;
import com.trodix.duckcloud.core.presentation.dto.requests.TagRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.TagResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    private final TagMapper tagMapper;

    @GetMapping("/tags/{id}")
    public TagResponse findOneTag(@PathVariable Long id) {
        Tag result = tagService.findOneTag(id);
        TagResponse response = tagMapper.toDto(result);
        return response;
    }

    @GetMapping("/tags")
    public List<TagResponse> findAllTags() {
        List<Tag> result = tagService.findAllTags();
        List<TagResponse> response = tagMapper.toDto(result);
        return response;
    }

    @PostMapping("/tags")
    public void createTag(@RequestBody TagRequest request) {

        final Tag data = tagMapper.toEntity(request);
        tagService.createTag(data);
    }

    @PutMapping("/tags/{id}")
    public void updateTag(@PathVariable Long id, @RequestBody TagRequest request) {

        final Tag data = tagMapper.toEntity(request);
        data.setId(id);
        tagService.updateTag(data);
    }

    @DeleteMapping("/tags/{id}")
    public void updateTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }

}
