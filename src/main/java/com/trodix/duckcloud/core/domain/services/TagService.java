package com.trodix.duckcloud.core.domain.services;

import com.trodix.duckcloud.core.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.core.persistance.entities.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TagService {

    private final TagMapper tagMapper;

    public Tag findOneTag(Long id) {
        return tagMapper.findOne(id);
    }

    public List<Tag> findAllTags() {
        return tagMapper.findAll();
    }

    public void createTag(Tag tag) {
        tagMapper.insert(tag);
    }

    public void updateTag(Tag tag) {
        tagMapper.update(tag);
    }

    public void deleteTag(Long id) {
        tagMapper.delete(id);
    }

}
