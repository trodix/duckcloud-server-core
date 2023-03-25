package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Tag;
import com.trodix.duckcloud.core.persistance.entities.Type;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

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
