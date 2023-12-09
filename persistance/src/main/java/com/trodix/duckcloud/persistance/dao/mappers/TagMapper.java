package com.trodix.duckcloud.persistance.dao.mappers;

import com.trodix.duckcloud.persistance.entities.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    Tag findOne(Long id);

    List<Tag> findAll();

    void insert(Tag tag);

    void update(Tag tag);

    void delete(Long id);

    List<Tag> findAllByNodeId(Long id);
}
