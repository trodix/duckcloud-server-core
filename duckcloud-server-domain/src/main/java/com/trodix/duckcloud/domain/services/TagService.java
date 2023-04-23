package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.persistance.dao.mappers.TagMapper;
import com.trodix.duckcloud.persistance.entities.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
