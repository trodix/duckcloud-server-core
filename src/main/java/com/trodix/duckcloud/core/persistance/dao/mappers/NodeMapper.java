package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Node;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NodeMapper {

    Node findOne(Long id);

    List<Node> findAll();

    void insert(Node node);

    void update(Node node);

    void delete(Long id);

}
